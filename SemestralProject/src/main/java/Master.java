import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Logger;

/**
 * Main model class of GUI app. Holds information about connected simulators.
 * Communicates with simulators via simulator connection class.
 */
public class Master implements Flow.Publisher{
    /** Constant representing maximal size of simulated world. */
    public static final int MAX_WORLD_SIZE = 2000000;
    /** Constant representing maximal population of simulated world. */
    public static final int MAX_POPULATION = 1000000;
    /** This classes logger.*/
    private static final Logger log = Logger.getLogger(Master.class.getName());
    /** Array of simulator connections. Contains all connected simulators. */
    private final SimulatorConnection[] simulators;
    /** Width and height of the simulated world. */
    private final Position worldSize;
    /** History of stats from all the rounds. */
    private final LinkedList<Stats> statsHistory;
    /** Most recent extract loaded from the simulators. */
    private Extract lastExtract;
    /** active rounds stats */
    private Stats currentStats;
    /** TopLeft corner of current extract focused area */
    private volatile Position extractTopLeft;
    /** size of current extract focused area */
    private volatile Position extractSize;
    /** List of all subscriptions associated with the master */
    private final List<MasterSubscription> subscriptions;
    /** Flag indicating an error occurred and simulation should be terminated */
    private volatile boolean error;

    /** Master class constructor
     * @param simulators simulators used to run the simulation.
     * @param worldSize size of the simulated world. */
    public Master(SimulatorConnection[] simulators, Position worldSize){
        this.simulators = simulators;
        this.worldSize = worldSize;
        statsHistory = new LinkedList<>();
        extractTopLeft = new Position(0,0);
        extractSize = worldSize;
        subscriptions = new LinkedList<>();
        error = false;
    }

    /** Executes a round of simulation.
     * <p>A round of simulation consists of moving people, infecting them,
     * healing them, getting general stats and needed extract and notifying subscribers</p>
     * <p>If there are no infected people left the subscribers will be notified through onComplete call.</p>
     */
    public void makeRound(){
        simsMovePeople();
        if(error) return;
        simsCalculateHealth();
        if(error) return;
        simsGetStats();
        if(error) return;
        simsGetExtract(extractTopLeft, extractSize);
        if(error) return;
        sendRoundEndedToSubscribers();
        if(currentStats.infected + currentStats.quarantined <= 0){
            sendCompleteToSubscribers();
        }
    }

    /** Sets the requested Extract area to passed values.
     * @param extractTopLeft topLeft corner of requested area.
     * @param extractSize size of requested area. */
    public synchronized void setExtractPosition(Position extractTopLeft, Position extractSize){
        this.extractTopLeft = extractTopLeft;
        this.extractSize = extractSize;
    }

    /** Stats history getter.
     * @return Linked list with stats after each round. */
    public LinkedList<Stats> getStatsHistory(){
        return statsHistory;
    }

    /**
     * World size getter.
     * @return Position - size of the simulated world.
     */
    public Position getWorldSize(){
        return worldSize;
    }

    /**
     * Returns latest extract received from the simulators.
     * It will only contain positions requested by the time it was obtained from simulators.
     * @return Extract - newest Extract of the simulated world.
     */
    public Extract getLastExtract(){
        return lastExtract;
    }

    /**
     * Obtains new extract of currently requested area from the simulators and stores it to latest extract
     */
    public void renewLastExtract(){
        simsGetExtract(extractTopLeft, extractSize);
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        new MasterSubscription(subscriber);
    }

    /**
     * Gets stats from all simulators, stores their sum to statsHistory.
     */
    public void simsGetStats(){
        currentStats = new Stats();
        Thread[] threads = new Thread[simulators.length];
        for(int i = 0; i < simulators.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    simulators[finalI].sendSendStatsCommand(this);
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
        statsHistory.add(currentStats);
    }

    /** Runs calculate health command on all simulators. */
    private void simsCalculateHealth(){
        Thread[] threads = new Thread[simulators.length];
        for(int i = 0; i < simulators.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    simulators[finalI].sendCalculateHealthCommand();
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
    }

    /** Runs move people command on all simulators. Handles cross-simulator exchange of people. */
    private void simsMovePeople(){
        Thread[] threads = new Thread[simulators.length];
        for(int i = 0; i < simulators.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    simulators[finalI].sendMoveCommand(this);
                    simulators[finalI].resolveWaitingPeople();
                    simulators[finalI].resolveWaitingConfirmations();
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
        simsResolveWaitingPeople();
        simsResolveWaitingConfirmations();
    }

    /** Makes all simulators respond to requests of people waiting to move to them. */
    private void simsResolveWaitingPeople(){
        Thread[] threads = new Thread[simulators.length];
        for(int i = 0; i < simulators.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    simulators[finalI].resolveWaitingPeople();
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
    }

    /** Makes all simulator connections send waiting person confirmations to their simulators. */
    private void simsResolveWaitingConfirmations(){
        Thread[] threads = new Thread[simulators.length];
        for(int i = 0; i < simulators.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    simulators[finalI].resolveWaitingConfirmations();
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
    }

    /**
     * Sends new contagion parameters to all simulators.
     * @param parameters new contagion parameters to use from now on.
     */
    public void simsUpdateContagionParameters(ContagionParameters parameters){
        Thread[] threads = new Thread[simulators.length];
        for(int i = 0; i < simulators.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    simulators[finalI].sendContagionParameters(parameters);
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
    }

    /** Requests extract of requested area from simulators and stores it in lastExtracct
     * @param topLeft topLeft corner of requested area.
     * @param size size of requested area.*/
    private void simsGetExtract(Position topLeft, Position size){
        Extract extract = new Extract(topLeft, size);
        Thread[] threads = new Thread[simulators.length];
        for(int i = 0; i < simulators.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    simulators[finalI].sendSendExtractCommand(topLeft, size, extract);
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
        lastExtract = extract;
    }

    /**
     * Adds passed stats to current stats.
     * @param stats stats to sum with current stats.
     */
    public synchronized void updateStats(Stats stats){
        currentStats.update(stats);
    }

    /** Accepts person from simulator connector and sends it to another simulator,
     * if target position doesn't exist, send negative confirmation.
     * @param person person to send.
     * @param targetPosition position that the person should land on.
     * @param origin Simulator to send the confirmation to. */
    public void forwardPerson(Person person, Position targetPosition, SimulatorConnection origin){
        for(SimulatorConnection sc : simulators){
            if(sc.containsPosition(targetPosition)){
                sc.addToWaitingPeople(new WaitingPerson(person, targetPosition, origin));
                return;
            }
        }
        //if no simulator contains target position
        origin.addToWaitingConfirmations(new WaitingPersonConfirmation(person.getPersonalNumber(), false));

    }

    /** Sets error flag and closes remaining connections in case of error. */
    private synchronized void handleCommunicationError(){
        if(!error){
            error = true;
            log.severe("Communication error occurred. Terminating simulation.");
            exitSims();
            sendErrorToSubscribers(new SimulatorConnectionException());
        }
    }

    /** Closes connection with the simulators and ends the simulators. */
    public void exitSims(){
        for( SimulatorConnection sim : simulators){
            sim.sendExitSim();
        }
    }

    /** Joins all thread in passed array. */
    private void waitForThreads(Thread[] threads){
        try{
            for(Thread t: threads){
                t.join();
            }
        }catch (InterruptedException e){
            log.severe("Threads interrupted.");
        }

    }

    /** Sends round ended notification to all subscribers. */
    private void sendRoundEndedToSubscribers(){
        synchronized (subscriptions){
            for(MasterSubscription ms : subscriptions){
                ms.sendRoundEnded();
            }
        }
    }

    /** Sends error notification to all subscribers. */
    private void sendErrorToSubscribers(Throwable t){
        synchronized (subscriptions){
            for(MasterSubscription ms : subscriptions){
                ms.sendError(t);
            }
        }
    }

    /** Sends on complete notification to all subscribers */
    private void sendCompleteToSubscribers(){
        synchronized (subscriptions){
            for(MasterSubscription ms : subscriptions){
                ms.sendComplete();
            }
        }
    }

    /** Inner class handling communication between master and subscribers. */
    public class MasterSubscription implements Flow.Subscription {

        private Subscriber subscriber;

        /**
         * Subscription constructor
         * @param subscriber subscriber associated with this subscription.
         */
        private MasterSubscription(Subscriber subscriber){
            this.subscriber = subscriber;
            synchronized (subscriptions){
                subscriptions.add(this);
            }
            subscriber.onSubscribe(this);
        }

        /**
         * calls onNext on subscriber and passes last rounds stats.
         */
        private void sendRoundEnded(){
            subscriber.onNext(statsHistory.getLast());
        }

        /**
         * Calls onError on subscriber and forwards the exception.
         * @param t Throwable object that caused or represents the error
         */
        private void sendError(Throwable t){
            subscriber.onError(t);
        }

        /** Calls subscribers onComplete method */
        private void sendComplete(){
            subscriber.onComplete();
        }

        @Override
        public void request(long n) {

        }

        @Override
        public void cancel() {
            synchronized (subscriptions){
                subscriptions.remove(this);
            }
        }
    }

}
