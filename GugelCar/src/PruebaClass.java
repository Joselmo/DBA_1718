import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

public class PruebaClass {

    public static void main(String[] args) {
        GugelCar gugelCar;

        // Conectarse a la plataforma
        AgentsConnection.connect("isg2.ugr.es", 6000,
                Mensajes.AGENT_HOST, Mensajes.AGENT_USER, Mensajes.AGENT_PASS, false);

        try {
            gugelCar = new GugelCar(new AgentID("GugelCarV3"));

            System.out.println("\n\n-------------------------------\n");
            gugelCar.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
