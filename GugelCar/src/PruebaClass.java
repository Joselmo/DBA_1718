import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

public class PruebaClass {

<<<<<<< HEAD
    int hola = 0;
    String ha = "Esto es una prueba de un commit -Diego, y esto es para forzar a que haya conflicto";
    String asdf = "asdfaasdasdadsa";
=======
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
>>>>>>> 473aabfaf9a285dfa606d27755e9823efaeb25ad
}
