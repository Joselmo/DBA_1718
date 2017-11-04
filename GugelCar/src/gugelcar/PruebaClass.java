package gugelcar;

import GUI.AgentNameCapture;
import GUI.GugelCarView;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

public class PruebaClass {

    public static void main(String[] args) {
        // Ventana de captura de nombre y mapa
        AgentNameCapture newCapture = new AgentNameCapture();
        newCapture.setVisible(true);

        GugelCarView gcv = new GugelCarView(newCapture.getMapaSeleccionado(),newCapture.getNombreAgente());

        gcv.setVisible(true);
    }
}