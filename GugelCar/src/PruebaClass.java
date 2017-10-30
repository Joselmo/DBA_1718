import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

import java.util.Scanner;

public class PruebaClass {

    public static void main(String[] args) {
        GugelCar gugelCar;
        Scanner scan = new Scanner(System.in);
        String map = null;

        // Leemos el mapa que queremos que recorra el guglecar
        System.out.print("Introduzca el nombre del mapa que quiere explorar (map + numero, ej: map1): ");
        while(map == null){
            map = scan.nextLine();
        }

        System.out.println("Mapa elegido = " + map);

        // Conectarse a la plataforma
        AgentsConnection.connect("isg2.ugr.es", 6000,
                Mensajes.AGENT_HOST, Mensajes.AGENT_USER, Mensajes.AGENT_PASS, false);

        try {
            gugelCar = new GugelCar(map, new AgentID("GugelCar"));

            System.out.println("\n\n-------------------------------\n");
            gugelCar.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
