import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

/** Main class of the simulator console app. */
public class MainSimulator {
    /** Constant representing default ip address. */
    private static final String IP_ADDRESS = "127.0.0.1";
    /** Constant representing default port. */
    private static final int PORT = 1666;

    /** Main method of simulator console app.
     * @param args Arguments to run the simulation with (1st can be IP_ADDRESS of maste, 2nd can be PORT of master)*/
    public static void main(String[] args) {
        //Get target address
        String ipAddress = args.length > 0 ? args[0] : IP_ADDRESS;
        String portStr = args.length > 1 ? args[1] : null;
        int port = PORT;
        if(portStr != null){
            try{
                port = Integer.parseInt(portStr);
            }catch (NumberFormatException e){
                port = PORT;
            }
        }

        //Connect to master
        Simulator simulator = new Simulator();
        Socket socket = new Socket();
        MasterConnection masterConnection = null;
        try{
            socket.connect(new InetSocketAddress(ipAddress, port));
            masterConnection = new MasterConnection(socket);
        }catch (IOException e){
            Logger.getLogger(SimulatorConnection.class.getName()).severe("Unable to connect to master.");
            System.out.println(
                    "Unable to connect to master on " + ipAddress + ":" + port + "\n" +
                    "The master is either not accepting simulators or running on different address and/or port\n" +
                    "You can connect simulator to any address by running the app with parameters: IP_ADDRESS PORT");
        }
        if(masterConnection == null) return;
        System.out.println("Connected.");

        //Handle masters requests
        while (masterConnection.listen(simulator));
    }

}
