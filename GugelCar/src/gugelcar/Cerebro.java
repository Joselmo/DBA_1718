package gugelcar;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import java.util.ArrayList;

class Cerebro {
    //DATOS MIEMBROS

    // Dato que nos indica si el agente a alcanzado el objetivo
    private boolean reachedGoal;

    // Elementos para la percepcion inmediata del agente
    private ArrayList<Integer> radarCar;    // Matriz que representa la percepcion del sensor radar
    private ArrayList<Integer> completeRadar;    // Matriz que representa la percepcion del sensor radar
    private ArrayList<Float> scannerCar;    // Matriz que representa la percepcion del sensor scanner
    private int bateriaCar;                 // Porcentaje de carga de la bateria

    // Memoria del mundo que ha pisado el agente y donde se encuentra actualmente
    private int [][] mapaPulgarcito;
    private int pos_fila_mapa;
    private int pos_col_mapa;

    // Memoria interna con las direcciones
    private final ArrayList<String> direcciones;

    // Atributos propios de Fantasmita(TM)
    private boolean fantasma_activo;
    private int [][] radarFantasmita;
    private int [][] mapaMundo;
    private int fantasmita_x;       // Variable X de origen del algoritmo
    private int fantasmita_y;       // Variable Y de origen del algoritmo

    // METODOS
    /**
     * Constructor del cerebro
     *
     * @author Andres Molina Lopez
     */
    Cerebro(){
        reachedGoal = false;
        fantasma_activo = false;

        // Inicializacion sensores
        radarCar = new ArrayList<>(9);
        completeRadar = new ArrayList<>(25);
        scannerCar = new ArrayList<>(9);
        bateriaCar = 0;     // Se inicializa a 0 puesto que desconocemos su estado real

        // Inicializacion del mapa pulgarcito y situacion del agente en el centro de su memoria del mundo
        mapaPulgarcito = new int[10001][10001];
        pos_fila_mapa = 5000;
        pos_col_mapa = 5000;

        // Inicializacion del mapa mundo
        radarFantasmita = new int[5][5];
        mapaMundo = new int[10001][10001];

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
    void processPerception(ArrayList<JsonObject> sensores){
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

                // Relleno del radar para la funcion fantasmita en el cual se usa el radar percibido al completo
                completeRadar.clear();
                for (int i = 0; i < 25; i++){
                    radarFantasmita[i / 5][i % 5] = radar.get(i).asInt();
                    completeRadar.add(radar.get(i).asInt());
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
    String nextAction(){
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
                entorno.add(scannerCar.get(i) + mapaPulgarcito[pos_fila_mapa + fil][pos_col_mapa + col] * 200);
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
    void refreshBatery(){
        bateriaCar = 100; // Como hemos repostado, la volvemos a poner al máximo
    }

    /**
     * Envia el siguiente movimiento, actualiza el mapa interno del agente y reduce la bateria
     *
     * @author Ángel Píñar Rivas, David Vargas Carrillo, Andrés Molina López
     * @param confirmacion indica si el resultado del movimiento fue válido
     * @param movimiento indica hacia donde se ha realizado el movimiento
     */
    void refreshMemory(boolean confirmacion, String movimiento){
        if (confirmacion) {
            // Se marca en la memoria que hemos pasado por la casilla
            mapaPulgarcito[pos_fila_mapa][pos_col_mapa]++;

            //////////////////////////////////////////////////////////////////////
            // ¿QUIZÁS ESTE CACHO (O PARTE DE ÉL) DEBERÍA ESTAR EN PROCESSPERCEPTION?
            // Despues de haber codificado esto me parece que se ha vuelto un metodo demasiado grande
            // dividlo como veais
            //////////////////////////////////////////////////////////////////////

            // @todo Por si acaso, que otra persona compruebe que escribe bien en mapaMundo
            // Ángel lo ha probado en el mapa 1 pero aun así es una persona insegura.

            //Escritura del mapaMundo
            //Que compruebe por si acaso que va a escribir en posiciones accesibles
            if((pos_fila_mapa > 2 && pos_fila_mapa < 9999) && (pos_col_mapa > 2 && pos_col_mapa < 9999)){
                //i, j iteran sobre mapaMundo. x,y iteran sobre matriz radar
                for(int i = pos_fila_mapa - 2, x = 0; i <= pos_fila_mapa + 2; i++, x++){
                    for(int j = pos_col_mapa - 2, y = 0; j <= pos_col_mapa + 2; j++, y++){
                        mapaMundo[i][j] = radarFantasmita[x][y]+1;
                    }
                }
            } else {
                System.err.println("Se ha intentado escribir en una posicion invalida de mapaMundo");
            }

            /* Seguramente haya una forma más sencilla de comprobar si al ver un objetivo es accesible o no, pero no se
            me ocurre.

            ¡¡OJO!! el código esta super incompleto y muy en sucio (EN DESARROLLO)
            así que no me vengais con tonterías que ya lo sé.

            Explicacion del (por ejemplo) lado izquierdo:
            El for recorre to.do el lado izquierdo, y por cada casilla:
            Comprueba si hay muro u objetivo por su derecha (el objetivo puede darse cuando se descubra en una esquina)
            Si lo hay:
                Comprueba si hay muro u objetivo por encima
                Si lo hay:
                    Comprueba si hay muro u objetivo por abajo
                    Si lo hay: Objetivo bloqueado (o su acceso aun no se ha descubierto)

             Nota del autor (Ángel): Esto da falsos positivos en los casos en los que se descubra 3 casillas de
             objetivo del tirón o dos casillas y una de ellas tenga un muro arriba o abajo (en el caso del ejemplo
             expuesto)

             Propuesta (ejemplificada para lado izquierdo)(posiblemente se pueda escribir el codigo mas simple):
             Recorrer el lado de arriba a abajo. Si encontramos casilla objetivo:
                if(por arriba hay muro o desconocido && por la derecha hay muro o desconocido){
                    if(por abajo hay muro){
                        dejar de recorrer lados (un break del for inicial o algo asi)
                        lanzar fantasmita y to.do el rollo
                    } else {
                        while(por abajo hay objetivo){
                            moverse_abajo
                            if(por derecha no hay muro)
                                salir del for(el objetivo está accesible)
                        }
                        if(por abajo hay muro o desconocido){
                            dejar de recorrer lados (un break del for inicial o algo asi)
                            lanzar fantasmita y to.do el rollo
                        }
                    }
             */







            /*
             * Ahora se va a comprobar si el fantasma se puede lanzar en el caso de que no este ya ejecutandose.
             * El objetivo es encontrar una situacion donde, a priori, el objetivo parezca inaccesible y haya que
             * comprobarlo a la hora de determinar una situacion GAMEOVER.
             */

            //////////////////////////////// TOCHACO CON IDEAS GUAYS DE MEJORA /////////////////////////////////////////

            // @todo mejora 1:
            // Poner esto en la funcion Fantasmita y llamar cada vez a la funcion, y que esta decida ahi si lo lanza
            // o no

            // @todo mejora 2:
            // Realmente se sabe de antemano hacia donde esta el objetivo, por tanto solo es necesario comprobar en uno
            // de los bordes y no recorrer los cuatro

            // @todo mejora 3:
            // Tambien es posible saber si tenemos la posibilidad de encontrarnos con el objetivo en nuestra area, a
            // partir de la distancia euclidea al mismo. Por tanto, definiendo una distancia max (que el objetivo se
            // encuentre en una de las esquinas) y comprobando la distancia a la que estamos mediante el scanner,
            // podemos saber si tenemos que lanzarnos a comprobar lo del fantasma o no

            ////////////////////////////// FIN TOCHACO CON IDEAS GUAYS DE MEJORA ///////////////////////////////////////
            // @todo comprobar la eficiencia de todo esto

            boolean lanzar_fantasma = false;

            if (!fantasma_activo) {
                // El algoritmo se divide en los cuatro bordes de la matriz, donde se pueda encontrar el objetivo
                // Borde izquierdo
                boolean seguir = true;
                boolean obj_encontrado = false;

                for (int fil = 0; fil < 5 && seguir; fil++) {
                    // Se busca la casilla objetivo
                    if ((radarFantasmita[fil][0] == 2) && (!obj_encontrado)) {
                        obj_encontrado = true;
                        // Se reinicia la fila para hacer una busqueda por toda la columna de la condicion Fantasmita
                        fil = 0;
                    }
                    // Cuando la casilla objetivo se encuentra en este borde
                    if (obj_encontrado) {
                        // Si la casilla superior es muro o desconocido y la casilla derecha es muro
                        if ((fil == 0 && radarFantasmita[fil][1] == 1) ||
                                radarFantasmita[fil - 1][0] == 1 && radarFantasmita[fil][1] == 1) {
                            // Si, estando en el objetivo, se encuentra un muro en la casilla inferior -> parar
                            // y lanzar fantasmita
                            if ((radarFantasmita[fil][0] == 2) && (fil < 4) && (radarFantasmita[fil + 1][0] == 1)) {
                                lanzar_fantasma = true;
                                seguir = false;
                            // Si no estamos en el objetivo o estando en el objetivo la casilla inferior no es un muro
                            } else {
                                // O bien es objetivo -> buscamos muros a la derecha
                                while ((fil < 4) && (radarFantasmita[fil + 1][0] == 2)) {
                                    // Nos movemos una casilla abajo
                                    fil++;
                                    // Comprobamos que a la derecha sigue habiendo muro
                                    if (radarFantasmita[fil][1] != 1) {
                                        // Si no hay, hay un acceso al objetivo
                                        seguir = false;
                                    }
                                }
                                // O bien es muro o calle -> buscamos el objetivo en casillas inferiores y si hay
                                // muro a la derecha
                                boolean obj_aqui = false;
                                boolean muro = false;
                                while ((fil < 4) && (radarFantasmita[fil + 1][1] == 1) && !muro && seguir) {
                                    fil++;
                                    // Encuentro el objetivo
                                    if (radarFantasmita[fil][0] == 2)
                                       obj_aqui = true;
                                    // En lugar del objetivo encuentro un muro que cierra el bloque -> paramos
                                    else if (radarFantasmita[fil][0] == 1) {
                                        muro = true;
                                        seguir = false;
                                    }
                                }
                                // Si se ha encontrado el objetivo antes que el muro, se lanza el fantasma
                                if (obj_aqui) {
                                    lanzar_fantasma = true;
                                    seguir = false;
                                }
                                // O bien es desconocido (hemos llegado al final del borde)
                                // Si se llega aqui es que el objetivo esta en la esquina. Podemos tomar dos decisiones,
                                // o dejar que el coche se aproxime una casilla mas o lanzar el fantasma ya
                                // Se opta por lanzar el fantasma
                                if (seguir && radarFantasmita[fil][0] == 2) {
                                    lanzar_fantasma = true;
                                    seguir = false;
                                }
                            }
                        }
                    }
                }
                // a partir de ahora comprobar tambien que el objetivo no esta encontrado en los for
                // Comprobar resto de bordes
            }
            // @todo implementar lanzamiento de fantasma
            if (lanzar_fantasma) {
                fantasma_activo = true;
                // Funcion Fantasmita
            }

            /*











            //@todo Implementar version mejor? Averiguar donde poner este cacho (no creo que este sea su sitio)

            if(!fantasma_activo) {
                for (int j = 0; j < 5; j++) { //Recorre el lado
                    if (radarFantasmita[0][j] == 2) { //lado izquierdo
                        if ((radarFantasmita[1][j] == 1) || radarFantasmita[1][j] == 2) {
                            if (j > 0 && ((radarFantasmita[0][j - 1] == 1) || (radarFantasmita[0][j - 1] == 2))) {
                                if (j < 4 && ((radarFantasmita[0][j + 1] == 1) || (radarFantasmita[0][j + 1] == 2))) {
                                    fantasma_activo = true;
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        }
                    }

                    if (radarFantasmita[5][j] == 2) { //lado derecho
                        if ((radarFantasmita[4][j] == 1) || (radarFantasmita[4][j] == 2)) {
                            if (j > 0 && ((radarFantasmita[5][j - 1] == 1) || (radarFantasmita[5][j - 1] == 2))) {
                                if (j < 4 && ((radarFantasmita[5][j + 1] == 1) || (radarFantasmita[5][j + 1] == 2))) {
                                    fantasma_activo = true;
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        }
                    }

                    if (radarFantasmita[j][0] == 2) { //lado superior
                        if ((radarFantasmita[j][1] == 0) || (radarFantasmita[j][1] == 2)) {
                            if (j > 0 && ((radarFantasmita[j - 1][0] == 1) || (radarFantasmita[j - 1][0] == 2))) {
                                if (j < 4 && ((radarFantasmita[j + 1][0] == 1) || (radarFantasmita[j + 1][0] == 2))) {
                                    fantasma_activo = true;
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        }
                    }

                    if (radarFantasmita[j][5] == 2) { //lado inferior
                        if ((radarFantasmita[j][4] != 0) && (radarFantasmita[j][4] != 2)) {
                            if (j > 0 && ((radarFantasmita[j - 1][5] == 1) || (radarFantasmita[j - 1][5] == 2))) {
                                if (j < 4 && ((radarFantasmita[j + 1][5] == 1) || (radarFantasmita[j + 1][5] == 2))) {
                                    fantasma_activo = true;
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        }
                    }
                }
            }*/
            /**/

            ///////////////////////////////////////////////////////////////
            // FIN DEL CACHO
            ///////////////////////////////////////////////////////////////

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

    /**
    * Algoritmo que escanea los bordes del muro proximo al objetivo con el fin de comprobar si hay GAMEOVER
     * @author David Vargas Carrillo, Angel Piñar Rivas
     * @return True si el objetivo no es alcanzable, false si el fantasma no está activo/no puede decir si es alcanzable
     *
     */
    boolean Fantasmita() {
        boolean camino_cerrado = false;
        if(fantasma_activo){
            //@todo El rollo de bordear el murito
            /*
            Sugerencia: hacer un """pulgarcito""" marcando por donde ha pasado (habría que crear una matriz
            nueva del mapa, sacamos un cacho de 50x50 o 100x100, no hace falta copiarla entera).
            El fantasmita buscara en sus 8 casillas adyacentes una de camino que esté junto
            a un muro visible por esas 8 casillas adyacentes (para evitar que se vaya a un muro del otro lado
            de la calle) y que además no haya pasado por ahí (para que no vuelva atrás).
            Si llega al punto de que solo puede ir a sitios marcados, quiere decir que ha dado la vuelta al muro
            y por tanto es inalcanzable (A NO SER QUE HAYA PASADO POR ENCIMA DEL OBJETIVO, este caso se podría dar
            y por tanto hay que controlarlo.)
             */
        }
        return camino_cerrado;
    }

    boolean isFantasmaActivo(){
        return fantasma_activo;
    }

    boolean hasReachedGoal() {
        return reachedGoal;
    }

    ArrayList<Float> getScannerCar() {
        return scannerCar;
    }

    ArrayList<Integer> getRadarCar() {
        return radarCar;
    }

    /**
     * Obtener el contenido completo del radar
     *
     * @author Diego Iáñez Ávila
     * @return El radar completo
     */
    ArrayList<Integer> getCompleteRadar(){
        return completeRadar;
    }

    int getPosX(){
        return pos_col_mapa;
    }

    int getPosY(){
        return pos_fila_mapa;
    }
}
