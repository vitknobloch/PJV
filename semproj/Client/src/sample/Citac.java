package sample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Citac extends JFrame {

    JButton upButton;
    JButton downButton;
    JButton resetButton;
    JLabel label;
    Controller controller;

    public Citac(String string){
        super(string);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400,300);
        LayoutManager mainLayout = new GridLayout(2,3);
        JPanel mainPanel = new JPanel(mainLayout);
        this.getContentPane().add(mainPanel);

        controller = new Controller();

        upButton = new JButton("up");
        downButton = new JButton("down");
        resetButton = new JButton("reset");
        label = new JLabel(controller.getCount() + "");

        mainPanel.add(new JPanel());
        mainPanel.add(label);
        mainPanel.add(new JPanel());
        mainPanel.add(upButton);
        mainPanel.add(downButton);
        mainPanel.add(resetButton);

        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.clickUp();
                label.setText(controller.getCount() + "");
            }
        });
        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.clickDown();
                label.setText(controller.getCount() + "");
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.clickReset();
                label.setText(controller.getCount() + "");
            }
        });
    }
}
