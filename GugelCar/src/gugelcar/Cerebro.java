package gugelcar;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;

public class Cerebro {
    //DATOS MIEMBROS
    // Elementos para la percepcion inmediata del agente
    private int numSensores;                        // Sensores que se estan utilizando en el momento
    private int [][] radarCar = new int[3][3];          // Matriz que representa la percepcion del sensor radar
    private float [][] scannerCar = new float[3][3];    // Matriz que representa la percepcion del sensor scanner
    private int bateriaCar = 0;                         // Porcentaje de carga de la bateria
    // Se inicializa a 0 puesto que desconocemos su estado real

    private int [][] mapaMundo = new int[10001][10001]; // Memoria del mundo que ha pisado el agente

    // Situamos al agente en el centro de su memoria del mundo
    private int pos_fila_mapa = 5000;
    private int pos_col_mapa = 5000;

    private boolean reachedGoal;

    // METODOS
    /**
     * Constructor del cerebro
     *
     * @author Andres Molina Lopez
     */
    public Cerebro(int numSens){
        reachedGoal = false;
        numSensores = numSens;
    }

    /**
     * Recibe y procesa la percepción del agente de los sensores y la almacena en los
     * sensores locales del cerebro
     *
     * @author Andrés Molina López, Diego Iáñez Ávila, Jose Luis Martínez Ortiz
     */
    public void processPerception(ArrayList<JsonObject> sensores){


        for (JsonObject msg : sensores){
            // Comprobamos si se está usando el radar y en caso afirmativo rellenamos su matriz de percepción
            if(msg.get(Mensajes.AGENT_COM_SENSOR_RADAR) != null) {
                JsonArray radar = msg.get(Mensajes.AGENT_COM_SENSOR_RADAR).asArray();
                int pos = 6;
                for (int i=0; i<3; i++){
                    for (int j=0; j<3; j++){
                        radarCar[i][j] = radar.get(pos).asInt();
                        pos++;
                    }
                    pos += 2;
                }
            }

            // Comprobamos si se está usando el scanner y en caso afirmativo rellenamos su matriz de percepción
            if (msg.get(Mensajes.AGENT_COM_SENSOR_SCANNER) != null){
                JsonArray scanner = msg.get(Mensajes.AGENT_COM_SENSOR_SCANNER).asArray();
                int pos = 6;
                for (int i=0; i<3; i++){
                    for (int j=0; j<3; j++){
                        scannerCar[i][j] = scanner.get(pos).asFloat();
                        pos++;
                    }
                    pos += 2;
                }
            }
        }

        /*  POR SI ALGUIEN QUIERE VER COMO SE ACTUALIZAN LAS PERCEPCIONES
        // Comprobación del contenido de radar
        System.out.println("\nContenido del radar: \n");
        for (int i=0; i<3; i++){
            System.out.println(radarCar[i][0] + " " + radarCar[i][1] + " " + radarCar[i][2] + "\n");
        }

        // Comprobación del contenido de scanner
        System.out.println("\nContenido del scanner: \n");
        for (int i=0; i<3; i++){
            System.out.println(scannerCar[i][0] + "     " + scannerCar[i][1] + "     " + scannerCar[i][2] + "     " + "\n");
        }
        */

        // Comprobamos si la posicion actual del coche es el objetivo
        if (radarCar[1][1] == 2) {
            reachedGoal = true;
        }
    }

    /**
     * Obtener la siguiente acción a realizar
     *
     * @author Diego Iáñez Ávila, Jose Luis Martínez Ortiz
     * @return El comando a ejecutar
     */
    public String nextAction(){
        String nextAction = Mensajes.AGENT_COM_ACCION_REFUEL;

        if(!reachedGoal && bateriaCar > 2)
            nextAction = findNextMove();

        return nextAction;
    }

    /**
     * Decide cual es el siguiente movimiento del agente
     *
     * @author Ángel Píñar Rivas, Jose Luis Martínez Ortiz
     * @return String con la siguiente dirección a la que ir
     */
    private String findNextMove(){
        String nextMove;

        float entorno[] = new float[9];
        int iter_entorno = 0;
        String direcciones[] = new String[9];
        direcciones[0] = Mensajes.AGENT_COM_ACCION_MV_NW;
        direcciones[1] = Mensajes.AGENT_COM_ACCION_MV_N;
        direcciones[2] = Mensajes.AGENT_COM_ACCION_MV_NE;
        direcciones[3] = Mensajes.AGENT_COM_ACCION_MV_W;
        direcciones[4] = "";
        direcciones[5] = Mensajes.AGENT_COM_ACCION_MV_E;
        direcciones[6] = Mensajes.AGENT_COM_ACCION_MV_SW;
        direcciones[7] = Mensajes.AGENT_COM_ACCION_MV_S;
        direcciones[8] = Mensajes.AGENT_COM_ACCION_MV_SE;

        //Almacenar en entorno los valores que detecta el radar
        for(int i=0 ; i<3 ; i++){
            for(int j=0 ; j<3 ; j++){
                if(radarCar[i][j] == 1)
                    entorno[iter_entorno] = Float.POSITIVE_INFINITY;
                else
                    entorno[iter_entorno] = scannerCar[i][j];
                iter_entorno++;
            }
        }

        /*
         *  Acceder al mapa desde pos_fila_mapa-1 y pos_col_mapa-1
         *  hasta pos_fila_mapa+1 y pos_col_mapa+1 y modificar los valores
         *  en el array entorno para el siguiente paso.
         */
        iter_entorno = 0;
        for(int i=-1; i < 2; i++) {
            for (int j = - 1; j < 2; j++) {
                entorno[iter_entorno] += mapaMundo[pos_fila_mapa + i][pos_col_mapa + j] * 50; // TODO arreglar valor a pelo

                iter_entorno++;
            }
        }

        System.out.print("Vector interno: {");

        float menor_valor = Float.POSITIVE_INFINITY;
        int direccion=4;

        for(int i=0 ; i < 9 ; i++){
            System.out.print(entorno[i] + ", ");

            if(entorno[i] < menor_valor && i != 4){
                menor_valor = entorno[i];
                direccion = i;
            }
        }

        System.out.println("}");

        nextMove = direcciones[direccion];
        return nextMove;
    }

    /**
     * Actualiza nuestra batería
     *
     * @author Ángel Píñar Rivas
     */
    public void refreshBatery(){
        bateriaCar = 100; // Como hemos repostado, la volvemos a poner al máximo
    }

    /**
     * Envia el siguiente movimiento, actualiza el mapa interno del agente y reduce la bateria
     *
     * @author Ángel Píñar Rivas, David Vargas Carrillo, Andrés Molina López
     * @param confirmacion indica si el resultado del movimiento fue válido
     * @param movimiento indica hacia donde se ha realizado el movimiento
     */
    public void refreshMemory(boolean confirmacion, String movimiento){
        if (confirmacion) {
            // Se marca en la memoria que hemos pasado por la casilla
            mapaMundo[pos_fila_mapa][pos_col_mapa]++;

            // Se desplaza la posicion en la matriz memoria segun el movimiento decidido
            switch (movimiento) {
                case Mensajes.AGENT_COM_ACCION_MV_NW:  // Movimiento noroeste
                    pos_fila_mapa--;
                    pos_col_mapa--;
                    break;
                case Mensajes.AGENT_COM_ACCION_MV_N:  // Movimiento norte
                    pos_fila_mapa--;
                    break;
                case Mensajes.AGENT_COM_ACCION_MV_NE:  // Movimiento noreste
                    pos_col_mapa++;
                    pos_fila_mapa--;
                    break;
                case Mensajes.AGENT_COM_ACCION_MV_W:  // Movimiento oeste
                    pos_col_mapa--;
                    break;
                case Mensajes.AGENT_COM_ACCION_MV_E:  // Movimiento este
                    pos_col_mapa++;
                    break;
                case Mensajes.AGENT_COM_ACCION_MV_SW:  // Movimiento suroeste
                    pos_fila_mapa++;
                    pos_col_mapa--;
                    break;
                case Mensajes.AGENT_COM_ACCION_MV_S:  // Movimiento sur
                    pos_fila_mapa++;
                    break;
                case Mensajes.AGENT_COM_ACCION_MV_SE:  // Movimiento sureste
                    pos_fila_mapa++;
                    pos_col_mapa++;
                    break;
                default:        // Ningun movimiento
                    // En el caso de que no se mueva, no pierde ningun punto de bateria
                    bateriaCar++;
                    break;
            }
            // Se resta un punto de bateria tras cada movimiento (en el caso normal)
            bateriaCar--;
        }
        else {
            System.err.println("Movimiento no permitido");
        }
    }

    public boolean hasReachedGoal() {
        return reachedGoal;
    }
}