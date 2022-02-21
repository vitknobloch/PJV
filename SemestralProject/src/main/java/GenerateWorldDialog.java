import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog class of dialog responsible for setting up simulated world.
 */
public class GenerateWorldDialog extends JDialog {
    private WorldGenerator worldGenerator;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner worldSizeXSpinner;
    private JSpinner worldSizeYSpinner;
    private JSpinner schoolCountSpinner;
    private JSpinner workplaceCountSpinner;
    private JSpinner restaurantCountSpinner;
    private JSpinner homeCountSpinner;
    private JSpinner populationSizeSpinner;
    private JSlider infectedRateSlider;
    private JSlider curedRateSlider;
    private JSlider vaccinatedRateSlider;
    private JSlider defaultSpreadSlider;
    private JSlider schoolMultiplierSlider;
    private JSlider workplaceMultiplierSlider;
    private JSlider restaurantMultiplierSlider;
    private JSlider homeMultiplierSlider;
    private JCheckBox masksCheckBox;
    private JSlider maskMultiplierSlider;
    private JCheckBox restaurantBanCheckBox;
    private JCheckBox workFromHomeCheckBox;
    private JCheckBox schoolFromHomeCheckBox;
    private JLabel infectedRateLabel;
    private JLabel curedRateLabel;
    private JLabel vaccinatedRateLabel;
    private JLabel defaultSpreadLabel;
    private JLabel schoolSpreadLabel;
    private JLabel workplaceSpreadLabel;
    private JLabel restaurantSpreadLabel;
    private JLabel homeSpreadLabel;
    private JLabel maskMultiplierLabel;
    private JSlider recoveryChanceSlider;
    private JSlider deathChanceSlider;
    private JSlider quarantineChanceSlider;
    private JLabel recoveryChanceLabel;
    private JLabel deathChanceLabel;
    private JLabel quarantineChanceLabel;

    /**
     * GenerateWorldDialog constructor.
     * @param worldGenerator world generator to set up from this dialog
     */
    public GenerateWorldDialog(WorldGenerator worldGenerator) {
        super((Dialog) null);
        this.worldGenerator = worldGenerator;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Generate world");

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e ->
                onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowOpened(e);
                setUpComponents();
            }
        });

        setUpListenersOnComponents();
    }

    private void onOK() {
        // add your code here
        if(!checkInputSemantics()) return;
        setUpGenerator();
        dispose();
    }

    private void setUpGenerator(){
        //World and locations
        int width = (int)worldSizeXSpinner.getValue();
        int height = (int)worldSizeYSpinner.getValue();
        int schoolsCount = (int)schoolCountSpinner.getValue();
        int workplacesCount = (int)workplaceCountSpinner.getValue();
        int restaurantsCount = (int)restaurantCountSpinner.getValue();
        int homeCount = (int)homeCountSpinner.getValue();

        //Population
        int populationSize = (int)populationSizeSpinner.getValue();
        float infectedRate = infectedRateSlider.getValue() / 1000f;
        float curedRate = curedRateSlider.getValue() / 1000f;
        float vaccinatedRate = vaccinatedRateSlider.getValue() / 1000f;

        //Spread
        float defaultSpreadChance = defaultSpreadSlider.getValue() / 1000f;
        float recoveryChance = recoveryChanceSlider.getValue() / 1000f;
        float deathChance = deathChanceSlider.getValue() / 1000f;
        float quarantineChance = quarantineChanceSlider.getValue() / 1000f;
        float schoolMultiplier = schoolMultiplierSlider.getValue() / 100f;
        float workplaceMultiplier = workplaceMultiplierSlider.getValue() / 100f;
        float restaurantMultiplier = restaurantMultiplierSlider.getValue() / 100f;
        float homeMultiplier = homeMultiplierSlider.getValue() / 100f;

        //Measures
        boolean masks = masksCheckBox.isSelected();
        float maskMultiplier = maskMultiplierSlider.getValue() / 1000f;
        boolean restaurantBan = restaurantBanCheckBox.isSelected();
        boolean workFromHome = workFromHomeCheckBox.isSelected();
        boolean schoolFromHome = schoolFromHomeCheckBox.isSelected();

        //Set up generator
        Position worldSize = new Position(width, height);
        ContagionParameters parameters = new ContagionParameters();
        parameters.defaultSpreadChance = defaultSpreadChance;
        parameters.recoveryChance = recoveryChance;
        parameters.deathChance = deathChance;
        parameters.quarantineChance = quarantineChance;
        parameters.restaurantsOpen = !restaurantBan;
        parameters.workOnSite = !workFromHome;
        parameters.schoolsOpen = !schoolFromHome;
        parameters.homeSpreadMultiplier = homeMultiplier;
        parameters.workplaceSpreadMultiplier = workplaceMultiplier;
        parameters.schoolSpreadMultiplier = schoolMultiplier;
        parameters.restaurantSpreadMultiplier = restaurantMultiplier;
        parameters.masks = masks;
        parameters.masksMultiplier = maskMultiplier;

        worldGenerator.setUpGenerator(worldSize,parameters,
                homeCount, restaurantsCount, schoolsCount, workplacesCount,
                populationSize, infectedRate, curedRate, vaccinatedRate);
    }

    private boolean checkInputSemantics(){
        boolean ret = true;

        //Check world size
        if(!checkWorldSize())
            return false;
        int width = (int)worldSizeXSpinner.getValue();
        int height = (int)worldSizeYSpinner.getValue();

        //check locations counts
        if(!checkLocationsCount(width, height))
            return false;

        //check population
        if(!checkPopulation())
            return false;


        //check infected rate
        if(!checkInitialHealthRates())
            return false;

        //check health change chances
        if(!checkHealthChangeChances())
            return false;

        //color green components where every input is correct
        defaultSpreadLabel.setForeground(new Color(51, 255, 51));
        schoolSpreadLabel.setForeground(new Color(51, 255, 51));
        workplaceSpreadLabel.setForeground(new Color(51, 255, 51));
        restaurantSpreadLabel.setForeground(new Color(51, 255, 51));
        homeSpreadLabel.setForeground(new Color(51, 255, 51));

        return true;
    }

    private boolean checkWorldSize(){
        boolean ret = true;
        int width = (int)worldSizeXSpinner.getValue();
        int height = (int)worldSizeYSpinner.getValue();
        if(width < 10 || width > Master.MAX_WORLD_SIZE){
            worldSizeXSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }else{
            worldSizeXSpinner.getEditor().getComponent(0).setBackground(new Color(179, 255, 179));
        }

        if(height < 10 || height > Master.MAX_WORLD_SIZE){
            worldSizeYSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }else{
            worldSizeYSpinner.getEditor().getComponent(0).setBackground(new Color(179, 255, 179));
        }

        if(!ret){
            JOptionPane.showMessageDialog(this,
                    "World size has to be between [10,10] and [" + Master.MAX_WORLD_SIZE + "," + Master.MAX_WORLD_SIZE + "]",
                    "Wrong input", JOptionPane.WARNING_MESSAGE);
        }

        return ret;
    }

    private boolean checkLocationsCount(int width, int height){
        boolean ret = true;
        int schoolsCount = (int)schoolCountSpinner.getValue();
        int workplacesCount = (int)workplaceCountSpinner.getValue();
        int restaurantsCount = (int)restaurantCountSpinner.getValue();
        int homeCount = (int)homeCountSpinner.getValue();

        if(homeCount < 1 || homeCount > width * height){
            homeCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }else{
            homeCountSpinner.getEditor().getComponent(0).setBackground(new Color(179, 255, 179));
        }

        if(restaurantsCount < 0 || restaurantsCount > width * height){
            restaurantCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }else{
            restaurantCountSpinner.getEditor().getComponent(0).setBackground(new Color(179, 255, 179));
        }

        if(workplacesCount < 0 || workplacesCount > width * height){
            workplaceCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }else{
            workplaceCountSpinner.getEditor().getComponent(0).setBackground(new Color(179, 255, 179));
        }

        if(schoolsCount < 0 || schoolsCount > width * height){
            schoolCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }else{
            schoolCountSpinner.getEditor().getComponent(0).setBackground(new Color(179, 255, 179));
        }

        if(ret && width * height < homeCount + schoolsCount + restaurantsCount + workplacesCount){
            homeCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            schoolCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            restaurantCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            workplaceCountSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }

        if(!ret){
            JOptionPane.showMessageDialog(this,
                    "There has to be at least one home and the sum of all position counts cannot exceed number of tiles.",
                    "Wrong input", JOptionPane.WARNING_MESSAGE);
        }

        return ret;
    }

    private boolean checkPopulation(){
        boolean ret = true;
        int populationSize = (int)populationSizeSpinner.getValue();
        if(populationSize < 1 || populationSize >  Master.MAX_POPULATION){
            populationSizeSpinner.getEditor().getComponent(0).setBackground(new Color(255,153,153));
            ret = false;
        }else{
            populationSizeSpinner.getEditor().getComponent(0).setBackground(new Color(179, 255, 179));
        }

        if(!ret){
            JOptionPane.showMessageDialog(this,
                    "Population has to be between 1 and " + Master.MAX_POPULATION,
                    "Wrong input", JOptionPane.WARNING_MESSAGE);
        }

        return ret;
    }

    private boolean checkInitialHealthRates(){
        boolean ret = true;

        int infectedRate = infectedRateSlider.getValue();
        int curedRate = curedRateSlider.getValue();
        int vaccinatedRate = vaccinatedRateSlider.getValue();
        if(curedRate + infectedRate + vaccinatedRate > 1000){
            infectedRateLabel.setForeground(new Color(255,51,51));
            curedRateLabel.setForeground(new Color(255,51,51));
            vaccinatedRateLabel.setForeground(new Color(255,51,51));
            ret = false;
        }
        else{
            infectedRateLabel.setForeground(new Color(51, 255, 51));
            curedRateLabel.setForeground(new Color(51, 255, 51));
            vaccinatedRateLabel.setForeground(new Color(51, 255, 51));
        }

        if(!ret){
            JOptionPane.showMessageDialog(this,
                    "Sum of initial rates of cured, infected and vaccinated people cannot exceed 100%.",
                    "Wrong input", JOptionPane.WARNING_MESSAGE);
        }

        return ret;
    }

    private boolean checkHealthChangeChances(){
        boolean ret = true;

        int recoveryChance = recoveryChanceSlider.getValue();
        int deathChance = deathChanceSlider.getValue();
        int quarantineChance = quarantineChanceSlider.getValue();
        if(recoveryChance + deathChance + quarantineChance > 1000){
            recoveryChanceLabel.setForeground(new Color(255,51,51));
            deathChanceLabel.setForeground(new Color(255,51,51));
            quarantineChanceLabel.setForeground(new Color(255,51,51));
            ret = false;
        }
        else{
            recoveryChanceLabel.setForeground(new Color(51, 255, 51));
            deathChanceLabel.setForeground(new Color(51, 255, 51));
            quarantineChanceLabel.setForeground(new Color(51, 255, 51));
        }
        if(!ret){
            JOptionPane.showMessageDialog(this,
                    "Sum of health change rates cannot exceed 100%.",
                    "Wrong input", JOptionPane.WARNING_MESSAGE);
        }

        return ret;
    }

    private void setUpComponents(){
        worldSizeXSpinner.setValue(100);
        worldSizeYSpinner.setValue(100);

        homeCountSpinner.setValue(100);
        schoolCountSpinner.setValue(15);
        restaurantCountSpinner.setValue(10);
        workplaceCountSpinner.setValue(30);

        populationSizeSpinner.setValue(500);
        infectedRateSlider.setValue(30);
        vaccinatedRateSlider.setValue(0);
        curedRateSlider.setValue(0);

        infectedRateLabel.setText(infectedRateSlider.getValue() / 10.0 + "%");
        curedRateLabel.setText(curedRateSlider.getValue() / 10.0 + "%");
        vaccinatedRateLabel.setText(vaccinatedRateSlider.getValue() / 10.0 + "%");

        defaultSpreadLabel.setText(String.format("%.1f%%", defaultSpreadSlider.getValue()/10.0));
        schoolSpreadLabel.setText(String.format("%.2fx", schoolMultiplierSlider.getValue()/100.0));
        schoolSpreadLabel.setText(String.format("%.2fx", schoolMultiplierSlider.getValue()/100.0));
        workplaceSpreadLabel.setText(String.format("%.2fx", workplaceMultiplierSlider.getValue()/100.0));
        restaurantSpreadLabel.setText(String.format("%.2fx", restaurantMultiplierSlider.getValue()/100.0));
        homeSpreadLabel.setText(String.format("%.2fx", homeMultiplierSlider.getValue()/100.0));
        recoveryChanceLabel.setText(String.format("%.1f%%", recoveryChanceSlider.getValue()/10.0));
        deathChanceLabel.setText(String.format("%.1f%%", deathChanceSlider.getValue()/10.0));
        quarantineChanceLabel.setText(String.format("%.1f%%", quarantineChanceSlider.getValue()/10.0));

        maskMultiplierLabel.setText(String.format("%.1f%%", maskMultiplierSlider.getValue()/10.0));
    }

    private void setUpListenersOnComponents(){
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        infectedRateSlider.addChangeListener(e -> infectedRateLabel.setText(infectedRateSlider.getValue() / 10.0 + "%"));
        curedRateSlider.addChangeListener(e -> curedRateLabel.setText(curedRateSlider.getValue() / 10.0 + "%"));
        vaccinatedRateSlider.addChangeListener(e -> vaccinatedRateLabel.setText(vaccinatedRateSlider.getValue() / 10.0 + "%"));
        defaultSpreadSlider.addChangeListener(e -> defaultSpreadLabel.setText(String.format("%.1f%%", defaultSpreadSlider.getValue()/10.0)));
        schoolMultiplierSlider.addChangeListener(e -> schoolSpreadLabel.setText(String.format("%.2fx", schoolMultiplierSlider.getValue()/100.0)));
        workplaceMultiplierSlider.addChangeListener(e -> workplaceSpreadLabel.setText(String.format("%.2fx", workplaceMultiplierSlider.getValue()/100.0)));
        restaurantMultiplierSlider.addChangeListener(e -> restaurantSpreadLabel.setText(String.format("%.2fx", restaurantMultiplierSlider.getValue()/100.0)));
        homeMultiplierSlider.addChangeListener(e -> homeSpreadLabel.setText(String.format("%.2fx", homeMultiplierSlider.getValue()/100.0)));
        maskMultiplierSlider.addChangeListener(e -> maskMultiplierLabel.setText(String.format("%.1f%%", maskMultiplierSlider.getValue()/10.0)));
        recoveryChanceSlider.addChangeListener(e -> recoveryChanceLabel.setText(String.format("%.1f%%", recoveryChanceSlider.getValue()/10.0)));
        deathChanceSlider.addChangeListener(e -> deathChanceLabel.setText(String.format("%.1f%%", deathChanceSlider.getValue()/10.0)));
        quarantineChanceSlider.addChangeListener(e -> quarantineChanceLabel.setText(String.format("%.1f%%", quarantineChanceSlider.getValue()/10.0)));

    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
