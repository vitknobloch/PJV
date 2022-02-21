import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.logging.Logger;

/**
 * Class handling connecting of simulators before the simulation.
 */
public class SimulatorConnectionAcceptor implements Flow.Publisher {
    private static final Logger log = Logger.getLogger(SimulatorConnectionAcceptor.class.getName());
    private final LinkedList<SimulatorConnection> sims;
    private volatile boolean stopAccepting;
    private final ServerSocket serverSocket;
    private final List<Subscription> subscriptions;

    /** Constant determining the port on which the server will be listening. */
    public static final int SERVER_PORT = 1666;

    /**
     * Constructor. Creates the server socket and binds it to SERVER_PORT
     * @throws IOException if socket creation fails.
     */
    public SimulatorConnectionAcceptor() throws IOException {
        sims = new LinkedList<>();
        stopAccepting = true;
        serverSocket = new ServerSocket(SERVER_PORT);
        subscriptions = new LinkedList<>();
    }

    /**
     * Returns the currently connected simulators in an array
     * @return SimulatorsConnection[] connected simulators.
     */
    public SimulatorConnection[] getSimulators(){
        synchronized (sims){
            SimulatorConnection[] array = new SimulatorConnection[sims.size()];
            sims.toArray(array);
            return array;
        }
    }

    /** Returns number of currently connected simulators.
     * @return int - currently connected simulators count */
    public int getSimulatorsCount(){
        synchronized (sims){
            return sims.size();
        }
    }

    /** Starts two threads. One is accepting new connections,
     * the other pinging connected simulators and removing them if they were disconnected. */
    public void startAccepting(){
        Thread acceptThread = new Thread(this::acceptRun);
        Thread pingThread = new Thread(this::pingRun);
        stopAccepting = false;
        acceptThread.start();
        pingThread.start();
    }

    /** Function running in accepting thread */
    private void acceptRun(){
        while(!stopAccepting) {
            try {
                //accept connections
                Socket simSocket = serverSocket.accept();
                synchronized (sims) {
                    sims.add(new SimulatorConnection(simSocket));
                    //notify subscribers
                    publishToAll();
                }
                System.out.println("Accepted");
            } catch (SocketException e) {
                //if stop accepting is true,
                //then the accept method was interrupted by stopAccepting() method
                if (!stopAccepting) {
                    log.severe("Server socket error.");
                    stopAccepting = true;
                    errorToAll(e);
                }
                break;
            } catch (IOException e) {
                log.severe("Error accepting socket.");
                stopAccepting = true;
                errorToAll(e);
                break;
            }
        }
    }

    /** Function running in pinging thread. */
    private void pingRun(){
        while(!stopAccepting){
            removeDisconnectedSims();
            try {
                Thread.sleep(1000); //Ping simulators once per second
            } catch (InterruptedException e) {
                log.severe("Ping run interrupted");
            }
        }
    }

    private void removeDisconnectedSims(){
        synchronized (sims){
            if(sims.size() == 0)
                return;

            //Get a list of disconnected simulators
            LinkedList<SimulatorConnection> disconnected = new LinkedList<>();
            for(SimulatorConnection sim: sims){
                if(!sim.pingSimulator()){
                    disconnected.add(sim);
                }
            }
            //Remove disconnected from active
            if(disconnected.size() > 0){
                for(SimulatorConnection sim : disconnected){
                    sims.remove(sim);
                    log.info("Disconnected sim removed.");
                }
                //Notify subscribers
                publishToAll();
            }
        }
    }

    /** Stops the accepting thread and pinging thread, pings connected simulators for check. */
    public void stopAccepting(){
        stopAccepting = true;
        try{
            if(!serverSocket.isClosed())
                serverSocket.close();
        }catch (IOException e){
            log.severe("Error closing socket.");
        }
        removeDisconnectedSims();
    }

    /** Ends all connected simulator applications and clears the list of them. */
    public void disconnectSimulators(){
        synchronized (sims){
            for(SimulatorConnection sim : sims){
                sim.sendExitSim();
            }
            sims.clear();
        }
    }

    /**
     * Notifies all subscribers of changed number of connected sims.
     */
    private void publishToAll(){
        for(Subscription s: subscriptions){
            s.publish();
        }
    }

    /**
     * Notifies all subscribers of an error.
     * @param t exceotion that caused the error.
     */
    private void errorToAll(Throwable t){
        for(Subscription s: subscriptions){
            s.sendError(t);
        }
    }

    /** Notifies all subscribers of successful completion. */
    private void CompleteToAll(){
        for(Subscription s: subscriptions){
            s.onComplete();
        }
    }

    @Override
    public void subscribe(Flow.Subscriber subscriber) {
        new Subscription(subscriber);

    }

    /** Class handling communication between acceptor and it's subscribers. */
    public class Subscription implements Flow.Subscription{
        private Flow.Subscriber subscriber;
        private int requested;

        private Subscription(Flow.Subscriber subscriber){
            this.subscriber = subscriber;
            requested = 0;
            subscriptions.add(this);
            subscriber.onSubscribe(this);
        }

        private synchronized void publish(){
            if(requested > 0){
                requested--;
                subscriber.onNext(getSimulatorsCount());
            }
        }

        private void sendError(Throwable t){
            subscriber.onError(t);
        }

        private void onComplete(){
            subscriber.onComplete();
        }

        @Override
        public void request(long n) {
            requested = (int) (requested + n);
        }

        @Override
        public void cancel() {
            subscriptions.remove(this);
        }
    }
}
