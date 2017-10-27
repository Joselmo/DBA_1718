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
     * Recibe y procesa la percepción del agente
     *
     * @author Andrés Molina López, Diego Iáñez Ávila
     */
    public void processPerception(ArrayList<JsonObject> sensores){
        for (JsonObject msg : sensores){
            // Comprobamos si se está usando el radar y en caso afirmativo rellenamos su matriz de percepción
            if(msg.get("radar") != null) {
                JsonArray radar = msg.get("radar").asArray();
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
            if (msg.get("scanner") != null){
                JsonArray scanner = msg.get("scanner").asArray();
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
     * @author Diego Iáñez Ávila
     * @return El comando a ejecutar
     */
    public String nextAction(){
        // @todo Tener en cuenta el caso en el que estemos en el objetivo
        String nextAction = Mensajes.AGENT_COM_ACCION_REFUEL;

        // Comprobamos que no se haya alcanzado el objetivo y que se tenga bateria
        if (!reachedGoal && bateriaCar > 2) {
            nextAction = findNextMove();
        }

        if (bateriaCar <= 2){
            nextAction = Mensajes.AGENT_COM_ACCION_REFUEL;
        }

        return nextAction;
    }

    /**
     * Decide cual es el siguiente movimiento del agente
     *
     * @author Ángel Píñar Rivas
     * @return String con la siguiente dirección a la que ir
     */
    private String findNextMove(){
        String nextMove;
        //@todo Implementar el algoritmo de seleccion de siguiente paso
        float entorno[][] = new float[3][3];

        for(int i=0 ; i<3 ; i++){
            for(int j=0 ; j<3 ; j++){
                if(radarCar[i][j] == 1)
                    entorno[i][j] = Float.POSITIVE_INFINITY;
                else
                    entorno[i][j] = scannerCar[i][j];
            }
        }
        //@todo Buscar los indices del menor valor de la matriz entorno
        int i, j;
        /**/
        if(i==0){ //norte
            if(j==0){
                //noroeste
            } else if(j==1){
                //norte
            } else {
                //noreste
            }
        }

        if(i==1){ //centro
            if(j==0){
                //oeste
            } else if(j==1){
                //centro
            } else {
                //este
            }
        }

        if(i==2){ //sur
            if(j==0){
                //suroeste
            } else if(j==1){
                //sur
            } else {
                //sureste
            }
        }
        /**/

        nextMove = "moveSW";
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
                case "moveNW":  // Movimiento noroeste
                    pos_fila_mapa--;
                    pos_col_mapa--;
                    break;
                case "moveN":  // Movimiento norte
                    pos_fila_mapa--;
                    break;
                case "moveNE":  // Movimiento noreste
                    pos_col_mapa++;
                    pos_fila_mapa--;
                    break;
                case "moveW":  // Movimiento oeste
                    pos_col_mapa--;
                    break;
                case "moveE":  // Movimiento este
                    pos_col_mapa++;
                    break;
                case "moveSW":  // Movimiento suroeste
                    pos_fila_mapa++;
                    pos_col_mapa--;
                    break;
                case "moveS":  // Movimiento sur
                    pos_fila_mapa++;
                    break;
                case "moveSE":  // Movimiento sureste
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
