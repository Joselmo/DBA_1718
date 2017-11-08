package practica2.gugelcar;

import practica2.GUI.AgentNameCapture;
import practica2.GUI.GugelCarView;

public class PruebaClass {

    public static void main(String[] args) {
        // Ventana de captura de nombre y mapa
        AgentNameCapture newCapture = new AgentNameCapture();
        newCapture.setVisible(true);

        GugelCarView gcv = new GugelCarView(newCapture.getMapaSeleccionado(),newCapture.getNombreAgente());

        gcv.setVisible(true);
    }
}