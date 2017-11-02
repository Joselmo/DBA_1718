package GUI;

import javax.swing.*;
import java.awt.event.*;

public class GugelCarView extends JFrame {
    private JButton buttonEjecutar;
    private JButton buttonSalir;
    private JLabel mapIndicator;
    private JPanel contentPane;
    private JPanel buttonsPanel;
    private JPanel informationPanel;
    private JPanel imgPanel;
    private JPanel GeneralMsgPanel;
    private JPanel ScannerPanel;
    private JPanel RadarPanel;
    private JTextArea scannerTextArea;
    private JTextArea generalMsgTextArea;
    private JTextArea radarTextArea;
    private JLabel traceLabel;


    /**
     * Constructor
     *
     * @author David Vargas Carrillo
     */
    public GugelCarView() {
        buttonEjecutar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEjecutar();
            }
        });

        buttonSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSalir();
            }
        });

        setContentPane(contentPane);
        setTitle("DBA Practica 3: gugelcar.gugelcar.GugelCar");
        setLocationRelativeTo(null);
        setSize(650, 400);
        initComponents();
    }

    /**
     * Constructor
     *
     * @author David Vargas Carrillo
     * @param mapa indicador del mapa actual (formato "mapX" o "mapXX")
     */
    public GugelCarView(String mapa) {
        setContentPane(contentPane);
        setTitle("DBA Practica 3: gugelcar.gugelcar.GugelCar");
        setSize(650, 400);
        setMapIndicator(mapa);
        initComponents();
    }

    /**
     * Inicializa los componentes de la clase
     *
     * @author David Vargas Carrillo
     */
    private void initComponents(){
        scannerTextArea.setEditable(false);
        radarTextArea.setEditable(false);
        generalMsgTextArea.setEditable(false);
        buttonEjecutar.setEnabled(false);
    }

    /**
     * Metodo SET para el indicador del mapa
     *
     * @author David Vargas Carrillo
     * @param mapInd String de la forma "mapX" o "mapXX" siendo X un entero
     */
    public void setMapIndicator(String mapInd) {
        mapIndicator.setText(mapInd);
    }

    /**
     * Imprimir texto en el panel Radar
     *
     * @author David Vargas Carrillo
     * @param radarText informacion recogida por el sensor radar
     */
    public void printToRadar(String radarText) {
        radarTextArea.setText(" ");
        radarTextArea.setText(radarText);
    }

    /**
     * Imprimir texto en el panel Scanner
     *
     * @author David Vargas Carrillo
     * @param scannerText informacion recogida por el sensor scanner
     */
    public void printToScanner(String scannerText) {
        scannerTextArea.setText(" ");
        scannerTextArea.setText(scannerText);
    }

    /**
     * Imprimir texto en el panel General Message
     *
     * @author David Vargas Carrillo
     * @param message mensaje que se quiere imprimir
     */
    public void printToGeneralMsg(String message) {
        generalMsgTextArea.setText(" ");
        generalMsgTextArea.setText(message);
    }

    /**
     * Imprimir la imagen de la traza
     *
     * @author David Vargas Carrillo
     * @param path ruta de la imagen de traza
     */
    public void printTraceUI(String path) {
        traceLabel.setText(" ");
        traceLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource(path)));
    }

    /**
     * Accion del boton de Ejecutar
     *
     * @author David Vargas Carrillo
     */
    private void onEjecutar(){
        // Completar
    }

    /**
     * Accion del boton de Salir
     *
     * @author David Vargas Carrillo
     */
    private void onSalir(){
        System.exit(0);
    }

}
