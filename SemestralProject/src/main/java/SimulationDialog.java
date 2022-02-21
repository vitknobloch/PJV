import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** JFrame class with frame controlling the simulation. */
public class SimulationDialog extends JFrame{
    private JPanel contentPane;
    private JPanel statsPanel;
    private JPanel parametersPanel;
    private JPanel viewPanel;
    private JPanel simControlPanel;
    private JCheckBox restaurantsCheckBox;
    private JCheckBox freeTimeCheckBox;
    private JCheckBox workCheckBox;
    private JCheckBox schoolCheckBox;
    private JCheckBox maskCheckBox;
    private JSlider maskMultiplierSlider;
    private JSlider defaultSpreadSlider;
    private JLabel maskMultiplierLabel;
    private JLabel spreadChanceLabel;
    private JLabel healthyLabel;
    private JLabel infectedLabel;
    private JLabel quarantinedLabel;
    private JLabel curedLabel;
    private JLabel deceasedLabel;
    private JButton nextStepButton;
    private JButton playButton;
    private JButton slowerButton;
    private JButton fasterButton;
    private ChartPanel statsChartPanel;
    private JPanel extractPanel;
    private JLabel simulationSpeedLabel;
    private JSlider recoveryChanceSlider;
    private JSlider deathChanceSlider;
    private JSlider quarantineChanceSlider;
    private JLabel quarantineChanceLabel;
    private JLabel deathChanceLabel;
    private JLabel recoveryChanceLabel;

    /** Extract displaying component */
    private ExtractDrawer extractDrawer;
    /** Stats chart drawing helper */
    private StatsDrawer statsDrawer;

    /** MasterController to use to communicate with master */
    private MasterController masterController;
    /** Flag indicating whether autoplay is toggled or not */
    private volatile boolean autoplaying;
    /** Currently set contagionParameters */
    private ContagionParameters contagionParameters;
    /** Currently active simulation autoplay speed */
    private SimulationSpeed simulationSpeed;

    /**
     * Simulation dialog constructor
     * @param master master to run the simulation on
     * @param parameters currently active contagion parameters
     */
    public SimulationDialog(Master master, ContagionParameters parameters) {
        super("Simulation");
        setContentPane(contentPane);

        contagionParameters = parameters;
        simulationSpeed = SimulationSpeed.NORMAL;

        extractDrawer = new ExtractDrawer(master.getWorldSize(), this);
        this.masterController = new MasterController(master,
                this,
                statsDrawer,
                extractDrawer,
                contagionParameters);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                setContagionParametersValues();
                simulationSpeedLabel.setText(simulationSpeed.getDisplayname());
                extractDrawer.updateExtractPositionAndSize();
                extractChanged();
                repaint();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });


        extractPanel.add(extractDrawer);
        nextStepButton.addActionListener(e -> {
            nextStepButton.setEnabled(false);
            masterController.makeStep();
        });
        playButton.addActionListener(e -> togglePlayButton());

        fasterButton.addActionListener(e -> {
            simulationSpeed = simulationSpeed.speedUp();
            slowerButton.setEnabled(true);
            if(simulationSpeed == SimulationSpeed.MAX)
                fasterButton.setEnabled(false);
            masterController.setAutoplayDelay(simulationSpeed.getTimeoutMs());
            simulationSpeedLabel.setText(simulationSpeed.getDisplayname());
        });

        slowerButton.addActionListener(e -> {
            simulationSpeed = simulationSpeed.slowDown();
            fasterButton.setEnabled(true);
            if(simulationSpeed == SimulationSpeed.SLOW)
                slowerButton.setEnabled(false);
            masterController.setAutoplayDelay(simulationSpeed.getTimeoutMs());
            simulationSpeedLabel.setText(simulationSpeed.getDisplayname());
        });

        ChangeListener chancesChangedListener = e -> chancesSliderChanged();
        recoveryChanceSlider.addChangeListener(chancesChangedListener);
        deathChanceSlider.addChangeListener(chancesChangedListener);
        quarantineChanceSlider.addChangeListener(chancesChangedListener);

        defaultSpreadSlider.addChangeListener(e -> {
            spreadChanceLabel.setText(String.format("%.1f%%",defaultSpreadSlider.getValue() / 10f));
            contagionParameters.defaultSpreadChance = defaultSpreadSlider.getValue()/1000f;
            masterController.updateContagionParameters(contagionParameters);
        });

        maskMultiplierSlider.addChangeListener(e -> {
            maskMultiplierLabel.setText(String.format("%.1f%%",maskMultiplierSlider.getValue() / 10f));
            contagionParameters.masksMultiplier = maskMultiplierSlider.getValue()/1000f;
            masterController.updateContagionParameters(contagionParameters);
        });

        maskCheckBox.addChangeListener(e -> {
            contagionParameters.masks = maskCheckBox.isSelected();
            masterController.updateContagionParameters(contagionParameters);
        });
        schoolCheckBox.addChangeListener(e -> {
            contagionParameters.schoolsOpen = !schoolCheckBox.isSelected();
            masterController.updateContagionParameters(contagionParameters);
        });
        workCheckBox.addChangeListener(e -> {
            contagionParameters.workOnSite = !workCheckBox.isSelected();
            masterController.updateContagionParameters(contagionParameters);
        });
        freeTimeCheckBox.addChangeListener(e -> {
            contagionParameters.freeTimeBan = freeTimeCheckBox.isSelected();
            masterController.updateContagionParameters(contagionParameters);
        });
        restaurantsCheckBox.addChangeListener(e -> {
            contagionParameters.restaurantsOpen = !restaurantsCheckBox.isSelected();
            masterController.updateContagionParameters(contagionParameters);
        });
    }

    /** Called when window is closing */
    private void onCancel(){
        masterController.windowClosed();
        dispose();
    }

    /**
     * Updates stats in the labels on top-left of frame
     * @param stats stats to change the labels to
     * */
    public void updateStatsLabels(Stats stats){
        deceasedLabel.setText(String.valueOf(stats.deceased));
        curedLabel.setText(String.valueOf(stats.cured));
        quarantinedLabel.setText(String.valueOf(stats.quarantined));
        healthyLabel.setText(String.valueOf(stats.healthy));
        infectedLabel.setText(String.valueOf(stats.infected));
    }

    /**
     * Notifies the masterController that requested extract area has changed.
     */
    public void extractChanged(){
        masterController.extractChanged();
    }

    /**
     * Enables next step button, set play/stop button to play
     */
    public void autoplayEnded(){
        nextStepButton.setEnabled(true);
        autoplaying = false;
        playButton.setText("Play");
    }

    /** Updates contagion parameters if new values are correct */
    private void chancesSliderChanged(){
        float recoveryChance = recoveryChanceSlider.getValue()/1000f;
        float deathChance = deathChanceSlider.getValue()/1000f;
        float quarantineChance = quarantineChanceSlider.getValue()/1000f;

        setTextChancesLabels();

        if(recoveryChance + deathChance + quarantineChance > 1.0){
            colorChancesLabels(new Color(255,51,51));
        }
        else{
            colorChancesLabels(Color.BLACK);
            //update contagion parameters values
            contagionParameters.recoveryChance = recoveryChance;
            contagionParameters.deathChance = deathChance;
            contagionParameters.quarantineChance = quarantineChance;
            //send new parameters to master
            masterController.updateContagionParameters(contagionParameters);
        }
    }

    private void colorChancesLabels(Color color){
        recoveryChanceLabel.setForeground(color);
        recoveryChanceLabel.setForeground(color);
        recoveryChanceLabel.setForeground(color);
    }

    private void setTextChancesLabels(){
        recoveryChanceLabel.setText(String.format("%.1f%%", recoveryChanceSlider.getValue() / 10f));
        deathChanceLabel.setText(String.format("%.1f%%", deathChanceSlider.getValue() / 10f));
        quarantineChanceLabel.setText(String.format("%.1f%%", quarantineChanceSlider.getValue() / 10f));
    }

    private void setContagionParametersValues(){
        restaurantsCheckBox.setSelected(!contagionParameters.restaurantsOpen);
        freeTimeCheckBox.setSelected(contagionParameters.freeTimeBan);
        workCheckBox.setSelected(!contagionParameters.workOnSite);
        schoolCheckBox.setSelected(!contagionParameters.schoolsOpen);
        maskCheckBox.setSelected(contagionParameters.masks);
        defaultSpreadSlider.setValue((int)(contagionParameters.defaultSpreadChance * 1000));
        spreadChanceLabel.setText(String.format("%.1f%%", defaultSpreadSlider.getValue() / 10f));
        maskMultiplierSlider.setValue((int)(contagionParameters.masksMultiplier * 1000));
        maskMultiplierLabel.setText(String.format("%.1f%%", maskMultiplierSlider.getValue() / 10f));
        recoveryChanceSlider.setValue((int)(contagionParameters.recoveryChance * 1000));
        deathChanceSlider.setValue((int)(contagionParameters.deathChance * 1000));
        quarantineChanceSlider.setValue((int)(contagionParameters.quarantineChance * 1000));
        setTextChancesLabels();
    }

    /** Controls what effect and visual the play/stop button has. */
    private void togglePlayButton(){
        if(autoplaying){
            autoplaying = false;
            masterController.setAutoplay(false);
            playButton.setText("Play");
        }else{
            autoplaying = true;
            masterController.setAutoplay(true);
            playButton.setText("Stop");
            nextStepButton.setEnabled(false);
            masterController.makeStep();
        }
    }

    private void createUIComponents() {
        statsDrawer = new StatsDrawer();
        statsChartPanel = new ChartPanel(statsDrawer.getChart());
    }

    /** inner private enum class for storing and setting the simulation autoplay speed */
    private enum SimulationSpeed{
        SLOW(2000, "0.5x"),
        NORMAL(1000, "1x"),
        FAST(500, "2x"),
        FASTER(200, "5x"),
        MAX(0, "Max");

        private final long timeout;
        private final String displayname;

        /**
         * Simulation speed constructor
         * @param timeoutMs timeout between autoplay steps with this SimulationSpeed
         * @param displayname text to show on speedLabel with this SimulationSpeed
         */
        SimulationSpeed(long timeoutMs, String displayname) {
            this.timeout = timeoutMs;
            this.displayname = displayname;
        }

        private long getTimeoutMs(){
            return timeout;
        }

        private String getDisplayname(){
            return displayname;
        }

        /** Get faster speed
         * @return one step faster SimulationSpeed */
        private SimulationSpeed speedUp(){
            if(this == MAX)
                return MAX;
            return values()[ordinal() + 1];
        }

        /** Get slower speed
         * @return one step slower simulation speed */
        private SimulationSpeed slowDown(){
            if(this == SLOW)
                return SLOW;
            return values()[ordinal() - 1];
        }
    }
}


