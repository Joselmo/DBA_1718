package gugelcar;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;

public class Cerebro {
    //DATOS MIEMBROS

    // Dato que nos indica si el agente a alcanzado el objetivo
    private boolean reachedGoal;

    // Elementos para la percepcion inmediata del agente
    private ArrayList<Integer> radarCar;    // Matriz que representa la percepcion del sensor radar
    private ArrayList<Float> scannerCar;    // Matriz que representa la percepcion del sensor scanner
    private int bateriaCar;                 // Porcentaje de carga de la bateria

    // Memoria del mundo que ha pisado el agente y donde se encuentra actualmente
    private int [][] mapaMundo;
    private int pos_fila_mapa;
    private int pos_col_mapa;

    // Memoria interna con las direcciones
    private final ArrayList<String> direcciones;

    // METODOS
    /**
     * Constructor del cerebro
     *
     * @author Andres Molina Lopez
     */
    public Cerebro(){
        reachedGoal = false;

        // Inicializacion sensores
        radarCar = new ArrayList<>(9);
        scannerCar = new ArrayList<>(9);
        bateriaCar = 0;     // Se inicializa a 0 puesto que desconocemos su estado real

        // Inicializacion del mapa mundo y situacion del agente en el centro de su memoria del mundo
        mapaMundo = new int[10001][10001];
        pos_fila_mapa = 5000;
        pos_col_mapa = 5000;

        // Inicializacion de las direcciones
        direcciones = new ArrayList<>(9);
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_NW);
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_N);
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_NE);
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_W);
        direcciones.add("");     // Es la posicion actual del coche
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_E);
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_SW);
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_S);
        direcciones.add(Mensajes.AGENT_COM_ACCION_MV_SE);
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
                radarCar.clear();
                for (int i=6; i<18; i+=3){
                    radarCar.add(radar.get(i).asInt());
                    radarCar.add(radar.get(++i).asInt());
                    radarCar.add(radar.get(++i).asInt());
                }
            }

            // Comprobamos si se está usando el scanner y en caso afirmativo rellenamos su matriz de percepción
            if (msg.get(Mensajes.AGENT_COM_SENSOR_SCANNER) != null){
                JsonArray scanner = msg.get(Mensajes.AGENT_COM_SENSOR_SCANNER).asArray();
                scannerCar.clear();
                for (int i=6; i<18; i+=3){
                    scannerCar.add(scanner.get(i).asFloat());
                    scannerCar.add(scanner.get(++i).asFloat());
                    scannerCar.add(scanner.get(++i).asFloat());
                }
            }
        }

        // Comprobamos si la posicion actual del coche es el objetivo
        if (radarCar.get(4) == 2) {
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
     * @author Ángel Píñar Rivas, Jose Luis Martínez Ortiz, Andrés Molina López
     * @return String con la siguiente dirección a la que ir
     */
    private String findNextMove(){
        String nextMove;
        ArrayList<Float> entorno = new ArrayList<>(9);

        // Almacenar en entorno los valores que el radar dectecta como 0 o 2, y poner a infinito los que detecta como 1
        int fil = -1, col = -1;
        for(int i=0 ; i<9 ; i++){
            if(radarCar.get(i) == 1) {
                entorno.add(Float.POSITIVE_INFINITY);
            }
            else {
                entorno.add(scannerCar.get(i) + mapaMundo[pos_fila_mapa + fil][pos_col_mapa + col] * 200);
            }

            // Para controlar la correlacion del vector entorno con la matriz del mapa del mundo interno del agente
            if(i == 2 || i == 5){
                fil++;
                col = -1;
            }
            else{
                col++;
            }
        }

        System.out.print("Vector entorno: {");

        float menor_valor = Float.POSITIVE_INFINITY;
        int direccion=4;

        for(int i=0 ; i < 9 ; i++){
            if (i < 8){
                System.out.print(entorno.get(i) + ", ");
            }
            else{
                System.out.print(entorno.get(i));
            }

            if(entorno.get(i) < menor_valor && i != 4){
                menor_valor = entorno.get(i);
                direccion = i;
            }
        }

        System.out.println("}");

        nextMove = direcciones.get(direccion);
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
