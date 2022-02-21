import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * Class encapsulating the network communication with one simulator from the masters side.
 */
public class SimulatorConnection {
    private static final Logger log = Logger.getLogger(SimulatorConnection.class.getName());
    private final Socket socket;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /** top-left corner of connected simulators area. */
    private Position topLeft;
    /** size of connected simulators area. */
    private Position size;
    /** List of confirmation waiting to be send to simulator. */
    private final LinkedList<WaitingPersonConfirmation> waitingConfirms;
    /** List of people wanting to visit the simulator and waiting for confirmation. */
    private final LinkedList<WaitingPerson> waitingPeople;

    /**
     * Simulator connection constructor. Extracts input and output streams from the socket.
     * @param socket socket connected to the simulator.
     * @throws IOException in case of error with extracting the input/output streams.
     */
    public SimulatorConnection(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new OutputStreamWriter(socket.getOutputStream());
        waitingConfirms = new LinkedList<>();
        waitingPeople = new LinkedList<>();
    }

    /**
     * Pings the simulator to find out if the simulator is still connected.
     * @return true if simulator is connected, false if communication fails. (doesn't throw exception)
     */
    public boolean pingSimulator(){
        try {
            sendString("Ping\n");
            String line = listen();
            if(!line.equals("Ping"))
                log.severe("Received unexpected string\nExpected: Ping\nReceived: " + line);
            return true;
        }catch (SimulatorConnectionException e){
            return false;
        }
    }

    /** Simulator connection constructor.
     * @param topLeft top-left corner of connected simulators area.
     * @param size size of connected simulators area.
     * @throws SimulatorConnectionException in case of error in communication between master and simulator.*/
    public void initSimulator(Position topLeft, Position size) throws SimulatorConnectionException {
        this.topLeft = topLeft;
        this.size=size;
        sendSetUp(topLeft, size);
    }

    /**
     * Adds a person to the list of waiting people
     * @param person person to add to the list
     */
    public void addToWaitingPeople(WaitingPerson person){
        synchronized (waitingPeople){
            waitingPeople.add(person);
        }
    }

    /**
     * Gets a person from the top of list of waiting people.
     * @return a waiting person if there are any, or null.
     */
    private WaitingPerson popFromWaitingPeople(){
        WaitingPerson wp;
        synchronized (waitingPeople){
            try{
                wp = waitingPeople.pop();
            }catch (NoSuchElementException e){
                wp = null;
            }
        }
        return wp;
    }

    /**
     * Sends all waiting people to the simulator and passes confirmations to the simulator of origin.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public void resolveWaitingPeople() throws SimulatorConnectionException {
        WaitingPerson wp = popFromWaitingPeople();
        while(wp != null){
            sendPerson(wp.getPerson(), wp.getTargetPosition(), wp.getOrigin());
            wp = popFromWaitingPeople();
        }
    }

    /**
     * Adds a confirmation to the list of waiting confirmations.
     * @param confirmation confirmation to add to the list
     */
    public void addToWaitingConfirmations(WaitingPersonConfirmation confirmation){
        synchronized (waitingConfirms){
            waitingConfirms.add(confirmation);
        }
    }

    /**
     * Remove a confirmation from the top of waiting confirmations list and returns it
     * @return a confirmation from waiting confirmations or null if there is none.
     */
    private WaitingPersonConfirmation popFromWaitingConfirmations(){
        WaitingPersonConfirmation wc;
        synchronized (waitingConfirms){
            try{
                wc = waitingConfirms.pop();
            }catch (NoSuchElementException e){
                wc = null;
            }
        }
        return wc;
    }

    /**
     * Sends all waiting confirmations to the simulator.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public void resolveWaitingConfirmations() throws SimulatorConnectionException {
        WaitingPersonConfirmation wc = popFromWaitingConfirmations();
        while(wc != null){
            sendPersonConfirmation(wc.getPersonalNumber(), wc.getConfirmation());
            wc = popFromWaitingConfirmations();
        }
    }

    /** Returns true if given position lies in connected simulators area.
     * @param position position to be in the are.
     * @return true if position is in connected simulator's area, false otherwise.*/
    public boolean containsPosition(Position position){
        return position.isInArea(topLeft, size);
    }

    /**
     * Waits for confirmations and/or data from the simulator and returns it
     * @return String - received string
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public String listen() throws SimulatorConnectionException {
        try {
            String line = reader.readLine();
            log.finer(line);
            return line;
        } catch (IOException e) {
            log.severe("Unable to receive data from simulator.");
            throw new SimulatorConnectionException();
        }
    }

    /**
     * Sends initial simulator set-up command with the worlds dimensions and the simulators position in the world
     * @param topLeft top-left corner of this simulators area
     * @param size size of this simulators area
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendSetUp(Position topLeft, Position size) throws SimulatorConnectionException {
        sendString("SetUp:" + topLeft.toString() + ":" + size.toString() + "\n");
        String confirmation = listen();
        if(!confirmation.equals("SimIsSet")){
            log.severe("Received unexpected string from simulator" +
                "\nReceived: " + confirmation + "\nExpected: SimIsSet");
        }
    }

    /**
     * Sends a person to the simulator.
     * @param person Person to send to the simulator.
     * @param targetPosition position to place the Person on.
     * @param origin Simulator connector to pass the confirmation to.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendPerson(Person person, Position targetPosition, SimulatorConnection origin) throws SimulatorConnectionException {
        sendString(person.toString(targetPosition) + "\n");
        String confirmation = listen();
        String[] split = confirmation.split(":");
        if(!split[0].equals("PersonConfirmation")){
            log.severe("Received unexpected string from simulator" +
                    "\nReceived: " + confirmation + "\nExpected: PersonConfirmation:...");
        }else{
            if (origin != null) {
                WaitingPersonConfirmation wc = new WaitingPersonConfirmation(Integer.parseInt(split[1]), Boolean.parseBoolean(split[2]));
                origin.addToWaitingConfirmations(wc);
            }else{
                if(!Boolean.parseBoolean(split[2])){
                    log.severe("Person sent without origin wasn't received by target simulator.");
                }
            }
        }
    }

    /**
     * Sends a confirmation to the simulator.
     * @param personalNumber unique identifier of the referenced person.
     * @param confirmation true if person was accepted by target location, false otherwise.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendPersonConfirmation(int personalNumber, boolean confirmation) throws SimulatorConnectionException {
        String confirmationStr = String.format("PersonConfirmation:%d:%b\n", personalNumber, confirmation);
        sendString(confirmationStr);
    }

    /**
     * Sends a Location to the simulator.
     * <p>
     *     This method does not transfer the locations visitors, all locations must be passed empty
     *     and then persons can be passed to it one by one.
     * </p>
     * @param location location to send.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendLocation(Location location) throws SimulatorConnectionException {
        sendString(location.toString() + "\n");
    }

    /**
     * Sends a command to the simulator, that makes the simulator move people in it's area.
     * @param master master instance to handle people transfers between simulators.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendMoveCommand(Master master) throws SimulatorConnectionException {
        sendString("MovePeople\n");
        String confirmation = listen();
        while(!confirmation.equals("PeopleMoved")){
            if(!confirmation.startsWith("Person:")){
                log.severe("Received unexpected string from simulator" +
                        "\nReceived: " + confirmation + "\nExpected: Person:...");
            }else{
                Person p = Person.parsePerson(confirmation);
                Position targetPos = Position.parsePosition(confirmation.split(":")[2]);
                master.forwardPerson(p, targetPos, this);
            }
            confirmation = listen();
        }
    }

    /**
     * Sends a command to the simulator, that makes the simulator calculate infection propagation.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendCalculateHealthCommand() throws SimulatorConnectionException {
        sendString("CalculateHealth\n");
        String confirmation = listen();
        if(!confirmation.equals("HealthCalculated")){
            log.severe("Received unexpected string from simulator" +
                    "\nReceived: " + confirmation + "\nExpected: HealthCalculated");
        }
    }

    /**
     * Sends new updated values of contagion parameters to the simulator
     * @param contagionParameters new contagion parameters
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendContagionParameters(ContagionParameters contagionParameters) throws SimulatorConnectionException {
        sendString(contagionParameters.toString() + "\n");
        String confirmation = listen();
        if(!confirmation.equals("ContagionParametersSet")){
            log.severe("Received unexpected string from simulator" +
                    "\nReceived: " + confirmation + "\nExpected: ContagionParametersSet");
        }
    }

    /**
     * Sends a command to the simulator to send back detailed information about locations in a certain area.
     * <p>This information is used to draw that area in output GUI window.</p>
     * @param topLeft topLeft corner of the displayed area.
     * @param size size of the displayed area.
     * @param extract extract to fill with received data
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendSendExtractCommand(Position topLeft, Position size, Extract extract) throws SimulatorConnectionException {
        sendString("SendExtract:" + topLeft.toString() + ":" + size.toString() + "\n");
        String received = listen();
        String[] receivedSplit = received.split(":");
        if(!receivedSplit[0].equals("Extract")){
            log.severe("Received unexpected string from simulator" +
                    "\nReceived: " + received + "\nExpected: Extract:...");
        }else{
            for(int i = 1; i < receivedSplit.length; i++){
                extract.addExtractedLocation(receivedSplit[i]);
            }
        }
    }

    /** Sends a command to the simulators that ends the simulator application. */
    public synchronized void sendExitSim(){
        try{
            sendString("ExitSim\n");
        }catch (SimulatorConnectionException e){
            log.info("Simulator with topLeft position " + topLeft.toString() + " unreachable.");
        }
    }

    /**
     * Sends a command to the simulator to send back general information about number of different health statuses in it's area.
     * <p>This information is used to run the basic overall statistics in GUI window.</p>
     * @param master master to update the stats in.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    public synchronized void sendSendStatsCommand(Master master) throws SimulatorConnectionException {
        sendString("SendStats\n");
        String received = listen();
        String[] receivedSplit = received.split(":");
        if(!receivedSplit[0].equals("Stats")){
            log.severe("Received unexpected string from simulator" +
                    "\nReceived: " + received + "\nExpected: Stats:...");
        }else{
            Stats stats = Stats.parseStats(receivedSplit[1]);
            master.updateStats(stats);
        }
    }

    /**
     * Sends the passed string to the master.
     * @param string string to send.
     * @throws SimulatorConnectionException in case connection to the simulator is lost.
     */
    private void sendString(String string) throws SimulatorConnectionException{
        log.finer(string);
        try {
            writer.write(string);
            writer.flush();
        } catch (IOException e) {
            log.severe("Connection error. Unable to send string:\n" + string);
            throw new SimulatorConnectionException();
        }
    }

}
