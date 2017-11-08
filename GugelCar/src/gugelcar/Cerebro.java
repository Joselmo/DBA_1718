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

            /*
            ¡¡OJO!! el código esta super incompleto y muy en sucio (EN DESARROLLO)
            así que no me vengais con tonterías que ya lo sé.
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

            if(!fantasma_activo){

                for(int i=0 ; i<5 ; i++){
                    if(radarFantasmita[i][0] == 2){ // Lado izquierdo

                        if(i==0){
                            if(radarFantasmita[i+1][0] == 1 && radarFantasmita[i+1][1] == 1 && radarFantasmita[i][1] == 1){ //esquina sup izq
                                fantasma_activo = true;

                                /* * /
                                Comprobar lado izquierdo libre y lado superior libre.
                                Coger uno de los libres y posicionar ahi el fantasmita
                                Si no hay libres, usar posicion del coche
                                /**/
                                if(mapaMundo[pos_fila_mapa][pos_col_mapa-1]==1){ //lado izquierdo (En mapamundo 0=desconocido 1=camino 2=muro 3=objetivo)
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa-1;
                                } else if(mapaMundo[pos_fila_mapa-1][pos_col_mapa]==1){
                                    fantasmita_x = pos_fila_mapa-1;
                                    fantasmita_y = pos_col_mapa;
                                } else{
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        } else if(i==4){
                            if(radarFantasmita[i-1][0] == 1 && radarFantasmita[i-1][1] == 1 && radarFantasmita[i][1] == 1){ //esquina inf izq
                                fantasma_activo = true;

                                //Comprobar izquierda y abajo
                                if(mapaMundo[pos_fila_mapa][pos_col_mapa-1]==1){
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa-1;
                                } else if(mapaMundo[pos_fila_mapa+1][pos_col_mapa]==1){
                                    fantasmita_x = pos_fila_mapa+1;
                                    fantasmita_y = pos_col_mapa;
                                } else{
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        } else{
                            comprobarColAccesible(0);
                        }
                    } else if(radarFantasmita[i][4] == 2){ // Lado derecho
                        if(i==0){
                            if(radarFantasmita[i+1][4] == 1 && radarFantasmita[i+1][3] == 1 && radarFantasmita[i][3] == 1){ //esquina sup der
                                fantasma_activo = true;

                                //Comprobar arriba y derecha
                                if(mapaMundo[pos_fila_mapa][pos_col_mapa+1]==1){
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa+1;
                                } else if(mapaMundo[pos_fila_mapa-1][pos_col_mapa]==1){
                                    fantasmita_x = pos_fila_mapa-1;
                                    fantasmita_y = pos_col_mapa;
                                } else{
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        } else if(i==4){
                            if(radarFantasmita[i-1][4] == 1 && radarFantasmita[i-1][3] == 1 && radarFantasmita[i][3] == 1){ //esquina inf der
                                fantasma_activo = true;

                                //Comprobar abajo y derecha
                                if(mapaMundo[pos_fila_mapa][pos_col_mapa+1]==1){
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa+1;
                                } else if(mapaMundo[pos_fila_mapa+1][pos_col_mapa]==1){
                                    fantasmita_x = pos_fila_mapa+1;
                                    fantasmita_y = pos_col_mapa;
                                } else{
                                    fantasmita_x = pos_fila_mapa;
                                    fantasmita_y = pos_col_mapa;
                                }
                            }
                        } else{
                            comprobarColAccesible(4);
                        }
                    } else if(radarFantasmita[0][i] == 2){ // Lado superior
                        comprobarFilaAccesible(0);
                    } else if(radarFantasmita[4][i] == 2){ // Lado inferior
                        comprobarFilaAccesible(4);
                    }
                }
            }

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
            System.out.println("\n\n\n\nHA LLAMADO AL FANTASMITA SOCORRO\n\n\n\n\n");
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


    /**
     * Busca el objetivo en una fila del radar y comprueba si es accesible
     *
     * @author Ángel Píñar Rivas, David Vargas Carrillo
     * @param numFila El indice de la fila que se va a comprobar
     */
    private void comprobarFilaAccesible(int numFila) {
        if(numFila==0 || numFila == 4) {
            boolean accesible = false;
            int fila_adyacente;
            boolean obstaculo_encontrado = false;
            boolean obj_encontrado = false;
            int lim_izq=0, lim_der=4;

            if (numFila == 0) {
                fila_adyacente = 1;
            } else {
                fila_adyacente = 3;
            }

            //Midiendo los bordes laterales de la fila
            for(int i=0 ; i<5 && !obj_encontrado ; i++){
                if(radarFantasmita[numFila][i] == 2){
                    obj_encontrado = true;
                    int j = i;
                    while(j>0 && !obstaculo_encontrado){
                        if(radarFantasmita[numFila][j-1] == 1){
                            obstaculo_encontrado = true;
                            lim_izq = j-1;
                        } else{
                            j--;
                            if(j==0){
                                lim_izq=0;
                                obstaculo_encontrado=true;
                            }
                        }
                    }

                    j=i;
                    obstaculo_encontrado = false;
                    while(j<4 && !obstaculo_encontrado){
                        if(radarFantasmita[numFila][j+1] == 1){
                            obstaculo_encontrado = true;
                            lim_der = j+1;
                        } else{
                            j++;
                            if(j==4){
                                lim_der=4;
                                obstaculo_encontrado=true;
                            }
                        }
                    }
                }
            }

            for(int i=lim_izq ; i<=lim_der && !accesible ; i++){
                if(radarFantasmita[fila_adyacente][i] != 1){
                    accesible = true;
                }
            }

            if(!accesible){
                fantasma_activo = true;
                fantasmita_x = pos_fila_mapa;
                fantasmita_y = pos_col_mapa;
            }
        }
    }


    /**
     * Busca el objetivo en una columna del radar y comprueba si es accesible
     *
     * @author Ángel Píñar Rivas, David Vargas Carrillo
     * @param numCol El indice de la columna que se va a comprobar
     */
    private void comprobarColAccesible(int numCol){
        if(numCol==0 || numCol == 4) {
            boolean accesible = false;
            int col_adyacente;
            boolean obstaculo_encontrado = false;
            boolean obj_encontrado = false;
            int lim_arr=0, lim_aba=4;

            if (numCol == 0) {
                col_adyacente = 1;
            } else {
                col_adyacente = 3;
            }

            //Midiendo los bordes laterales de la fila
            for(int i=0 ; i<5 && !obj_encontrado ; i++){
                if(radarFantasmita[i][numCol] == 2){
                    obj_encontrado = true;
                    int j = i;
                    while(j>0 && !obstaculo_encontrado){
                        if(radarFantasmita[j-1][numCol] == 1){
                            obstaculo_encontrado = true;
                            lim_arr = j-1;
                        } else{
                            j--;
                            if(j==0){
                                lim_arr=0;
                                obstaculo_encontrado=true;
                            }
                        }
                    }

                    j=i;
                    obstaculo_encontrado = false;
                    while(j<4 && !obstaculo_encontrado){
                        if(radarFantasmita[j+1][numCol] == 1){
                            obstaculo_encontrado = true;
                            lim_aba = j+1;
                        } else{
                            j++;
                            if(j==4){
                                lim_aba=4;
                                obstaculo_encontrado=true;
                            }
                        }
                    }
                }
            }

            for(int i=lim_arr ; i<=lim_aba && !accesible ; i++){
                if(radarFantasmita[i][col_adyacente] != 1){
                    accesible = true;
                }
            }

            if(!accesible){
                fantasma_activo = true;
                fantasmita_x = pos_fila_mapa;
                fantasmita_y = pos_col_mapa;
            }
        }
    }
}
