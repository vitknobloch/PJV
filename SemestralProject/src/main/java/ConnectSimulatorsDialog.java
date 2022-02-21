import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Flow;

/** Dialog class of a dialog used to display process of connecting the simulators. */
public class ConnectSimulatorsDialog extends JDialog implements Flow.Subscriber {
    private JPanel contentPane;
    private JLabel connectedCountLabel;
    private JLabel warningLabel;
    private JButton buttonOK;
    private SimulatorConnectionAcceptor acceptor;
    private Flow.Subscription acceptorSubscription;

    /**
     * Constructor of the Connect simulators dialog.
     * @param acceptor SimulationConnectionAcceptor to handle the actual connecting.
     */
    public ConnectSimulatorsDialog(SimulatorConnectionAcceptor acceptor){
        super((Dialog) null);
        initComponents();
        setModalityType(ModalityType.APPLICATION_MODAL);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Connect simulators");

        buttonOK.addActionListener(e -> onOK());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        //start acceptor
        this.acceptor = acceptor;
        acceptor.startAccepting();
        acceptor.subscribe(this);
        updateConnectedCount(0);
    }


    private void initComponents(){
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel descriptionLabel = new JLabel("Connect simulator apps which will run the simulation.");
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(descriptionLabel);
        contentPane.add(Box.createRigidArea(new Dimension(0,5)));

        warningLabel = new JLabel("You have to connect at least one simulator!");
        warningLabel.setForeground(new Color(255,51,51));
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(warningLabel);
        contentPane.add(Box.createRigidArea(new Dimension(0,7)));

        connectedCountLabel = new JLabel("Currently connected simulators: 0");
        connectedCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Font connectedLabelFont = connectedCountLabel.getFont();

        contentPane.add(connectedCountLabel);
        contentPane.add(Box.createRigidArea(new Dimension(0,10)));

        buttonOK = new JButton("OK");
        buttonOK.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPane.add(buttonOK);
    }

    private void onOK(){
        acceptor.stopAccepting();
        if(acceptor.getSimulatorsCount() <= 0){
            return;
        }
        dispose();
    }

    private void onCancel(){
        acceptor.stopAccepting();
        acceptor.disconnectSimulators();
        dispose();
    }

    private void updateConnectedCount(int count){
        connectedCountLabel.setText("Currently connected simulators: " + count);
        if(count > 0){
            warningLabel.setVisible(false);
            buttonOK.setEnabled(true);
        }else{
            warningLabel.setVisible(true);
            buttonOK.setEnabled(false);
        }
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        acceptorSubscription = subscription;
        acceptorSubscription.request(1);
    }

    @Override
    public void onNext(Object item) {
        updateConnectedCount((int)item);
        acceptorSubscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        JOptionPane.showMessageDialog(this,
                "A fatal error occurred while connecting simulators.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    @Override
    public void onComplete() {
        onOK();
    }
}
