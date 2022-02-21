import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;

/** Dialog class displaying the progress of world generation. */
public class WorldGeneratingProgressDialog extends JDialog implements Subscriber {
    private JPanel contentPane;
    private JLabel descriptionLabel;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    private Flow.Subscription subscription;

    /**
     * WorldGeneratingProgressDialog constructor
     * @param generator World generator whose progress to display.
     */
    public WorldGeneratingProgressDialog(WorldGenerator generator) {
        super((Dialog) null);
        initComponents();
        setContentPane(contentPane);
        setTitle("Generating world");
        this.setModal(false);

        generator.subscribe(this);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
    }

    /** Creates GUI */
    private void initComponents(){
        contentPane = new JPanel(new GridLayout(3,1, 0, 5));
        contentPane.setBorder(new EmptyBorder(10, 20, 5, 20));
        descriptionLabel = new JLabel("Generating world in progress.");
        progressBar = new JProgressBar();
        progressBar.setSize(350, 30);
        statusLabel = new JLabel("Setting up simulators...");

        contentPane.add(descriptionLabel);
        contentPane.add(progressBar);
        contentPane.add(statusLabel);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Object item) {
        int stage = (int)item;

        switch (stage){
            case 1 -> {
                statusLabel.setText("Setting up contagion parameters...");
                progressBar.setValue(10);
            }
            case 2 -> {
                statusLabel.setText("Generating homes...");
                progressBar.setValue(20);
            }
            case 3 -> {
                statusLabel.setText("Generating people");
                progressBar.setValue(30);
            }
            case 4 -> {
                statusLabel.setText("Sending homes...");
                progressBar.setValue(40);
            }
            case 5 -> {
                statusLabel.setText("Generating and sending schools...");
                progressBar.setValue(50);
            }
            case 6 -> {
                statusLabel.setText("Generating and sending workplaces.");
                progressBar.setValue(60);
            }
            case 7 -> {
                statusLabel.setText("Generating and sending restaurants.");
                progressBar.setValue(70);
            }
            case 8 -> {
                statusLabel.setText("Sending people...");
                progressBar.setValue(80);
            }
        }
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        JOptionPane.showMessageDialog(this,
                "Lost connection to one or more simulators while generating world.\n" +
                        "The simulation will be terminated.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        dispose();
    }

    @Override
    public void onComplete() {
        dispose();
    }
}
