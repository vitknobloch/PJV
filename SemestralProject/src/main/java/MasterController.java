import javax.swing.*;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Logger;

/** Class handling communication between SimulationDialog and Master */
public class MasterController implements Subscriber{

    private static final Logger log = Logger.getLogger(MasterController.class.getName());

    private final Master master;
    private final SimulationDialog simulationDialog;
    private Flow.Subscription subscription;
    private final StatsDrawer statsDrawer;
    private final ExtractDrawer extractDrawer;

    /** Current simulation round. */
    private int roundNumber;
    /** Flag indicating that there are no infected people left and user has already been notified*/
    private boolean simulationComplete;
    /** System time when last round simulation begun. */
    private long lastStepTimeMs;
    /** Minimal time between executing two rounds if autoplay is on. */
    private long autoplayDelayMs;
    /** Flag indicating that master is currently communicating with the Simulators on another thread
     * ordering master to communicate with simulators while roundInProgress is true will likely result in an error. */
    private volatile boolean roundInProgress;
    /** Flag indicating that rounds should be executed automatically after some delay. */
    private volatile boolean autoplay;
    /** Flag indicating that contagion parameters have changed since executing last round
     * and need to be sent to simulators before executing new round. */
    private volatile boolean contagionParametersChanged;
    /** Currently active contagion parameters. */
    private ContagionParameters contagionParameters;

    /**
     * Master controller constructor.
     * @param master master that runs the simulation.
     * @param dialog frame that displays the simulation.
     * @param statsDrawer Component that draws the stat-chart.
     * @param extractDrawer Component that draws the graphical representation of extract.
     * @param parameters Initially active contagion parameters.
     */
    public MasterController(Master master,
                            SimulationDialog dialog,
                            StatsDrawer statsDrawer,
                            ExtractDrawer extractDrawer,
                            ContagionParameters parameters){
        //assign local properties
        this.master = master;
        this.statsDrawer = statsDrawer;
        this.extractDrawer = extractDrawer;
        this.simulationDialog = dialog;
        this.contagionParameters = parameters;

        //subscribe to master
        master.subscribe(this);

        //init flags
        simulationComplete = false;
        autoplay = false;
        autoplayDelayMs = 1000;
        contagionParametersChanged = true;

        //setup initial stats
        master.simsGetStats();
        Stats stats = master.getStatsHistory().getLast();
        simulationDialog.updateStatsLabels(stats);
        statsDrawer.drawStats(stats, roundNumber++);
    }

    /**
     * Executes a single round of simulation on the master
     */
    public void makeStep(){
        if(!waitOnRoundEnd())
            return;

        boolean paramsChanged = contagionParametersChanged;
        contagionParametersChanged = false;
        Thread roundThread = new Thread(() -> {
            if(paramsChanged){
                master.simsUpdateContagionParameters(contagionParameters);
            }
            master.makeRound();
        });
        lastStepTimeMs = System.currentTimeMillis();
        roundThread.start();
    }

    /** Waits for round to end if autoplay is on and returns false if autoplay is off and round is in progress.
     *  returns true otherwise. */
    private synchronized boolean waitOnRoundEnd(){
        if(roundInProgress) {
            if(!autoplay)
                return false;
            else{
                while (roundInProgress) {
                    Thread.onSpinWait();
                    //Busy wait for extract to be extracted.
                }
            }
        } else {
            roundInProgress = true;
        }
        return true;
    }

    /** Updates active contagion parameters and sets flag to update it in simulators before executing next round.
     * @param parameters new contagion parameters. */
    public void updateContagionParameters(ContagionParameters parameters){
        contagionParameters = parameters;
        contagionParametersChanged = true;
    }

    /**
     * Updates delay between rounds when autoplay is on to passed value of milliseconds.
     * If the round takes longer than the delay to execute the rounds will be executed as soon as the previous ends.
     * @param milliseconds time in milliseconds to wait between executing two rounds.
     */
    public void setAutoplayDelay(long milliseconds){
        autoplayDelayMs = milliseconds;
    }

    /**
     * Sets flag indicating whether rounds should be executed automatically.
     * @param play true if next round should be executed after current round ends, false otherwise.
     */
    public void setAutoplay(boolean play){
        autoplay = play;
    }

    /**
     * Updates requested extract area in master and if round is not currently running renews the extract straight away.
     */
    public void extractChanged(){
        //update requested area (even if round is in progress)
        master.setExtractPosition(
                extractDrawer.getRequestedExtractTopLeft(),
                extractDrawer.getRequestedExtractSize());
        //check if round is in progress
        synchronized (this){
            if(roundInProgress) {
                return;
            } else {
                roundInProgress = true;
            }
        }
        //renew extract if round is not in progress
        master.renewLastExtract();
        Extract extract = master.getLastExtract();
        extractDrawer.setExtract(extract);
        roundInProgress = false;
    }

    /** Handles ending the simulation when user closes the simulation window. */
    public void windowClosed(){
        autoplay = false;
        while (roundInProgress) {
            Thread.onSpinWait();
            //busy wait for master to be free
        }
        master.exitSims();
    }

    /**
     * Updates components reflecting the simulation. (Stats labels, stats chart, extract panel)
     * @param stats latest rounds stats to update the components with.
     */
    public void updateComponents(Stats stats){
        simulationDialog.updateStatsLabels(stats);
        statsDrawer.drawStats(stats, roundNumber++);
        extractDrawer.setExtract(master.getLastExtract());
        extractDrawer.repaint();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Object item) {
        Stats stats = (Stats)item;

        //update components
        Thread UIthread = new Thread(()->updateComponents(stats));
        UIthread.start();

        roundInProgress = false;

        tryAutoplay();
        if(!autoplay){
            //enable next step execution if autoplay is not active and round is not active.
            simulationDialog.autoplayEnded();
        }

        subscription.request(1);
    }

    /**
     * Makes next step if autoplay is on. Otherwise it does nothing.
     * Executes the next round straight away or waits for delay to pass.
     */
    private void tryAutoplay(){
        if(autoplay){
            if(autoplayDelayMs < System.currentTimeMillis() - lastStepTimeMs){
                makeStep();
            } else {
                try {
                    Thread.sleep(autoplayDelayMs - (System.currentTimeMillis() - lastStepTimeMs));
                } catch (InterruptedException e) {
                    log.severe("Waiting for autoplay interrupted.");
                }
                if(autoplay)
                    makeStep();
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        JOptionPane.showMessageDialog(simulationDialog,
                "Lost communication with one or more of the simulators.\nThe simulation will be terminated.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    @Override
    public void onComplete() {
        //only notify user once.
        if(simulationComplete) return;
        simulationComplete = true;

        autoplay = false; //pause autoplaying simulation
        JOptionPane.showMessageDialog(simulationDialog,
                "There are no infected people left,\nthe infection has been eradicated.",
                "Simulation complete",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
