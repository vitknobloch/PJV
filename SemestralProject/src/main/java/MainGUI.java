import javax.swing.*;
import java.io.IOException;

/** Main class of the Master application */
public class MainGUI {
    /** Main method of the graphical application
     * @param args arguments of the program (unused) */
    public static void main(String[] args){
        //Open for simulator connection
        SimulatorConnectionAcceptor acceptor;
        try {
            acceptor = new SimulatorConnectionAcceptor();
        }catch (IOException e){
            acceptor = null;
            JOptionPane.showMessageDialog(null,
                    "Error opening masters connection",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        //Connect simulators
        ConnectSimulatorsDialog connectSimulatorsDialog = new ConnectSimulatorsDialog(acceptor);
        connectSimulatorsDialog.pack();
        connectSimulatorsDialog.setResizable(false);
        connectSimulatorsDialog.setVisible(true);
        SimulatorConnection[] simulatorConnections = acceptor.getSimulators();
        if(simulatorConnections.length == 0)
            System.exit(0);


        //set up world generator
        WorldGenerator worldGenerator = new WorldGenerator(simulatorConnections);
        GenerateWorldDialog generateWorldDialog = new GenerateWorldDialog(worldGenerator);
        generateWorldDialog.pack();
        generateWorldDialog.setVisible(true);
        if(!worldGenerator.isSetUp()){
            acceptor.disconnectSimulators();
            System.exit(0);
        }

        //generate world
        WorldGeneratingProgressDialog progressDialog = new WorldGeneratingProgressDialog(worldGenerator);
        progressDialog.pack();
        progressDialog.setVisible(true);
        worldGenerator.generateWorld();
        if(worldGenerator.raisedError()) {
            return;
        }

        //start up main simulation window
        Master master = new Master(simulatorConnections, worldGenerator.getWorldSize());
        SimulationDialog simulationDialog = new SimulationDialog(master, worldGenerator.getContagionParameters());
        simulationDialog.pack();
        simulationDialog.setMinimumSize(simulationDialog.getSize());
        simulationDialog.setVisible(true);
    }
}
