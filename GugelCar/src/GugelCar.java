import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

import java.io.FileOutputStream;
import java.io.IOException;

public class GugelCar extends SingleAgent{

    private String password;
    private AgentID controllerID;
    private boolean reachedGoal; //@todo borrar esta variable

    // Elementos para la percepcion inmediata del agente
    private int numSensores = 2;
    private int [][] radarCar = new int[3][3];
    private float [][] scannerCar = new float[3][3];
    private int bateriaCar = 100;

    // Memoria del mundo que ha pisado el agente
    private int [][] mapaMundo = new int[10001][10001];
    private int pos_fila_mapa = 5000;   // Situamos al agente en medio del mundo
    private int pos_col_mapa = 5000;


    /**
     * Constructor
     *
     * @author Diego Iáñez Ávila
     * @param aid ID del agente
     * @throws Exception si no puede crear el agente
     */
    public GugelCar(AgentID aid) throws Exception {
        super(aid);

        controllerID = new AgentID("Girtab");
        reachedGoal = false;
    }

    /**
     * Método de inicialización del agente
     *
     * @author Diego Iáñez Ávila
     */
    @Override
    public void init(){
        // Loguearse en el mapa 1
        JsonValue map = Json.value("map1");
        JsonValue agentID = Json.value(getAid().toString());

        JsonObject jsonLogin = Json.object();
        jsonLogin.add("command", "login");
        jsonLogin.add("world", "map1");
        jsonLogin.add("radar", agentID);
        jsonLogin.add("scanner", agentID);

        sendMessage(jsonLogin.toString());

        // Recibir y guardar la contraseña
        try {
            password = null;
            while(password == null) {
                JsonObject answer = receiveJson();
                password = answer.getString("result", null);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cuerpo del agente
     *
     * @author Diego Iáñez Ávila, Andrés Molina López
     */
    @Override
    public void execute(){
        // Cuando esté implementado de verdad, la condición de salida del bucle no será esta
        // y por lo tanto no se comprobará dos veces como ahora.
        while (!reachedGoal) {
            processPerception();

            // Comprobamos que no se haya alcanzado el objetivo y que se tenga bateria
            if (!reachedGoal && bateriaCar > 2) {
                sendCommand("moveSW");
                mapaMundo[pos_fila_mapa][pos_col_mapa] += 1; // Marcamos que hemos pasado por esa casilla
                pos_fila_mapa -= 1; // Desplazmos la posición según el movimiento hecho
                pos_col_mapa -= 1;
                bateriaCar -= 1; // Reducimos la batería
            }

            if (bateriaCar <= 2){
                sendCommand("refuel");
                bateriaCar = 100; // Como hemos repostado, la volvemos a poner al máximo
            }
        }

        // Terminar sesión
        endSession();
    }

    /**
     * Recibe y procesa la percepción del agente
     *
     * @author Andrés Molina López
     */
    private void processPerception(){
        try {
            // Recibimos los mensajes del servidor en orden
            int sensoresRecibidos = 0;
            while(sensoresRecibidos < numSensores){
                JsonObject msg = receiveJson();
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

                sensoresRecibidos++;
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

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finaliza la sesión con el controlador
     *
     * @author Diego Iáñez Ávila
     */
    private void endSession(){
        // Desloguearse
        System.out.println("Terminando sesión");

        sendCommand("logout");
        processPerception();

        try{
            System.out.println("Recibiendo traza");
            JsonObject injson = receiveJson();
            JsonArray ja = injson.get("trace").asArray();

            byte data[] = new byte[ja.size()];

            for (int i = 0; i < data.length; ++i){
                data[i] = (byte) ja.get(i).asInt();
            }

            FileOutputStream fos = new FileOutputStream("traza_" + password + ".png");
            fos.write(data);
            fos.close();
            System.out.println("Traza guardada en " + "traza_" + password + ".png");

        } catch (InterruptedException | IOException ex){
            System.err.println("Error procesando traza");
        }
    }

    /**
     * Enviar un mensaje al controlador
     *
     * @author Diego Iáñez Ávila
     * @param message Mensaje a enviar
     */
    private void sendMessage(String message){
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(controllerID);
        outbox.setContent(message);

        send(outbox);
    }

    /**
     * Envía un comando al controlador
     *
     * @author Diego Iáñez Ávila
     * @param command Comando a enviar
     * @return true si el controlador respondió con OK al comando
     */
    private boolean sendCommand(String command){
        boolean success = true;

        JsonObject jsonCommand = Json.object();
        jsonCommand.add("command", command);
        jsonCommand.add("key", password);

        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setReceiver(controllerID);
        outbox.setContent(jsonCommand.toString());

        send(outbox);

        try{
            JsonObject answer = receiveJson();
            String result = answer.getString("result", "BAD_MESSAGE");

            if (!result.equals("OK"))
                success = false;

        } catch (InterruptedException e){
            success = false;
        }

        return success;
    }

    /**
     * Recibir un mensaje del controlador en formato JSON
     *
     * @author Diego Iáñez Ávila
     * @return El JSON recibido
     * @throws InterruptedException Si hay error al recibir el mensaje
     */
    private JsonObject receiveJson() throws InterruptedException {
        ACLMessage inbox = receiveACLMessage();
        System.out.println("Recibido mensaje " + inbox.getContent());

        return Json.parse(inbox.getContent()).asObject();
    }
}
