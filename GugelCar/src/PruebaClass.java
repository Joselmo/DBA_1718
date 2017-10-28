import GUI.AgentNameCapture;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

public class PruebaClass {

    public static void main(String[] args) {
        GugelCar gugelCar;

        // Ventana de captura de nombre y mapa
        AgentNameCapture newCapture = new AgentNameCapture();
        newCapture.setVisible(true);

        String mapaSeleccionado = newCapture.getMapaSeleccionado();
        String nombreAgente = newCapture.getNombreAgente();

        // Conectarse a la plataforma
        AgentsConnection.connect("isg2.ugr.es", 6000,
                Mensajes.AGENT_HOST, Mensajes.AGENT_USER, Mensajes.AGENT_PASS, false);

        // @todo implementar mecanismos para parar la ejecucion en caso de que se reciban erores tipo BAD_MAP
        try {
            gugelCar = new GugelCar(new AgentID(nombreAgente), mapaSeleccionado);

            System.out.println("\n\n-------------------------------\n");
            gugelCar.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}