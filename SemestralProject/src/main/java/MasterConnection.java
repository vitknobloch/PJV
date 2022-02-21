import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Class that simulators use to communicate with master application via network.
 */
public class MasterConnection {
    private Logger log = Logger.getLogger(MasterConnection.class.getName());
    private Socket socket;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    /**
     * Master connection constructor. Extracts input and output streams from the socket and stores them for later use.
     * @param socket Socket connected to the Master.
     * @throws IOException If there is an error opening the input/output streams.
     */
    public MasterConnection(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new OutputStreamWriter(socket.getOutputStream());
    }

    /** Sends person in predefined string format.
     * @param person person to send.
     * @param targetPosition position that the person should land on.*/
    public void sendPerson(Person person, Position targetPosition){
        sendString(person.toString(targetPosition) + "\n");
    }

    /** Sends confirmation of accepting person by location in this simulators area
     * @param personalNumber personal number of referenced person.
     * @param confirmation true if person was accepted, false if person was rejected. */
    public void sendPersonConfirmation(int personalNumber, boolean confirmation){
        String confirmationStr = String.format("PersonConfirmation:%d:%b\n", personalNumber, confirmation);
        sendString(confirmationStr);
    }

    /** Sends information about Locations in the area.
     * @param area Array of Location in the requested area. */
    public void sendExtract(Location[] area){
        StringBuilder extractStr = new StringBuilder("Extract");
        if(area != null){
            for(Location l : area){
                Stats locStats = l.getStats();
                extractStr.append(String.format(":%s;%s;%s", l.getPosition().toString(), l.getTypeSting(), locStats.toString()));
            }
        }
        extractStr.append("\n");
        sendString(extractStr.toString());
    }

    /** Sends general statistical information to the simulator.
     * @param stats stats to send. */
    public void sendStats(Stats stats){
        sendString("Stats:" + stats.toString() + "\n");
    }

    /** Sends a confirmation to the master that this simulator has ended moving people. */
    public void sendPeopleMovedConfirmation(){
        sendString("PeopleMoved\n");
    }

    /** Sends a confirmation to the master that this simulator has ended calculating peoples health. */
    public void sendHealthCalculatedConfirmation(){
        sendString("HealthCalculated\n");
    }

    /** Sends a confirmation to the master that sim is set up */
    private void sendSimIsSetConfirmation(){sendString("SimIsSet\n");}

    /**
     * Sends a string to the Master
     * @param  string to send
     */
    private void sendString(String string){
        try {
            writer.write(string);
            writer.flush();
        } catch (IOException e) {
            log.severe("Connection error. Unable to send data to master.");
        }
    }

    /** Accepts commands from master application and executes them using respective methods of simulator.
     * @param simulator simulator class to execute commands from master application.
     * @return true until receiving ExitSim command or until losing connection to the master, then return false.
     */
    public boolean listen(Simulator simulator){
        //read line from input stream
        String received = listenLine();
        if(received == null) return false;
        String[] receivedSplit = received.split(":", -1);

        //extract command type and execute it
        switch (receivedSplit[0]) {
            case "Person" -> {
                Person person = Person.parsePerson(received);
                Position targetPostion = Position.parsePosition(receivedSplit[2]);
                sendPersonConfirmation(person.getPersonalNumber(), simulator.addPerson(person, targetPostion));
            }
            case "PersonConfirmation" -> {
                int personalNumber = Integer.parseInt(receivedSplit[1]);
                boolean confirmation = Boolean.parseBoolean(receivedSplit[2]);
                simulator.confirmPerson(personalNumber, confirmation);
            }
            case "Location" -> {
                Location loc = Location.parseLocation(received);
                if (loc != null)
                    simulator.addLocation(loc);
            }
            case "MovePeople" -> {
                simulator.movePeople(this);
                sendPeopleMovedConfirmation();
            }
            case "CalculateHealth" -> {
                simulator.calculateHealth();
                sendHealthCalculatedConfirmation();
            }
            case "SendExtract" -> {
                Position topLeft = Position.parsePosition(receivedSplit[1]);
                Position size = Position.parsePosition(receivedSplit[2]);
                Location[] area = simulator.getArea(topLeft, size);
                sendExtract(area);
            }
            case "SendStats" -> {
                Stats stats = simulator.getStats();
                sendStats(stats);
            }
            case "ContagionParameters" -> {
                ContagionParameters parameters = ContagionParameters.parseContagionParameters(received);
                simulator.setContagionParameters(parameters);
                sendString("ContagionParametersSet\n");
            }
            case "SetUp" -> {
                Position topLeft = Position.parsePosition(receivedSplit[1]);
                Position size = Position.parsePosition(receivedSplit[2]);
                simulator.setSimulator(topLeft, size);
                sendSimIsSetConfirmation();
            }
            case "Ping" -> {
                sendString("Ping\n");
            }
            case "ExitSim" -> {
                log.info("Received ExitSim command, exiting simulator.");
                return false;
            }
            default -> {
                log.severe("Received unexpected string from master\nMessage: " + received);
            }
        }
        return true;
    }

    /** Reads a line from input stream.
     * @return String - line received from master, or null in case of error. */
    private String listenLine(){
        try {
            return reader.readLine();
        } catch (IOException e) {
            log.severe("Cannot receive data from master.");
        }
        return null;
    }


}
