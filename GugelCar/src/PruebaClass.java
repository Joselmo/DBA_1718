import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

public class PruebaClass {

    public static void main(String[] args) {
        GugelCar gugelCar;

        // Conectarse a la plataforma
        AgentsConnection.connect("isg2.ugr.es", 6000, "Girtab", "Eridano", "Esquivel", false);

        try {
            gugelCar = new GugelCar(new AgentID("GugelCar2"));

            System.out.println("\n\n-------------------------------\n");
            gugelCar.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
