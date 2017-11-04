package gugelcar;

import GUI.AgentNameCapture;
import GUI.GugelCarView;

public class PruebaClass {

    public static void main(String[] args) {
        // Ventana de captura de nombre y mapa
        AgentNameCapture newCapture = new AgentNameCapture();
        newCapture.setVisible(true);

        GugelCarView gcv = new GugelCarView(newCapture.getMapaSeleccionado(),newCapture.getNombreAgente());

        gcv.setVisible(true);
    }
}