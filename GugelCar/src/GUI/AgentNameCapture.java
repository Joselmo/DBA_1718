package GUI;

import javax.swing.*;
import java.awt.event.*;
import java.util.Vector;

public class AgentNameCapture extends JDialog {
    private JPanel contentPane;
    private JButton loginButton;
    private JButton buttonCancel;
    private JFormattedTextField agentNameField;
    private JComboBox mapSelectionBox;

    public AgentNameCapture() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(loginButton);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // Configurar mapSelectionBox
        mapSelectionBox.addItem(makeObj("map1")); mapSelectionBox.addItem(makeObj("map2"));
        mapSelectionBox.addItem(makeObj("map3")); mapSelectionBox.addItem(makeObj("map4"));
        mapSelectionBox.addItem(makeObj("map5")); mapSelectionBox.addItem(makeObj("map6"));
        mapSelectionBox.addItem(makeObj("map7")); mapSelectionBox.addItem(makeObj("map8"));
        mapSelectionBox.addItem(makeObj("map9")); mapSelectionBox.addItem(makeObj("map10"));

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private Object makeObj(final String item) {
        return new Object() { public String toString() { return item; } };
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        AgentNameCapture dialog = new AgentNameCapture();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
