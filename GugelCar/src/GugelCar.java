import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GugelCar extends SingleAgent{

    private String password;
    private AgentID controllerID;
    private Cerebro cerebro;
    private int numSensores;

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
    }

    /**
     * Método de inicialización del agente
     *
     * @author Diego Iáñez Ávila
     */
    @Override
    public void init(){
        // Loguearse en el mapa 1
        JsonValue agentID = Json.value(getAid().toString());

        JsonObject jsonLogin = Json.object();
        jsonLogin.add("command", "login");
        jsonLogin.add("world", "map1");
        jsonLogin.add("radar", agentID);
        jsonLogin.add("scanner", agentID);

        numSensores = 2;
        cerebro = new Cerebro(numSensores);

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
        while (!cerebro.hasReachedGoal()) {
            processPerception();

            // Comprobamos que no se haya alcanzado el objetivo y que se tenga bateria
            if (!reachedGoal && bateriaCar > 2) {
                String nextMove = findNextMove();
                makeMove(nextMove);

                /*
                sendCommand("moveSW");
                mapaMundo[pos_fila_mapa][pos_col_mapa] += 1; // Marcamos que hemos pasado por esa casilla
                pos_fila_mapa -= 1; // Desplazmos la posición según el movimiento hecho
                pos_col_mapa -= 1;
                bateriaCar -= 1; // Reducimos la batería
                */
            }

            if (bateriaCar <= 2){
                refuel();

                /*
                sendCommand("refuel");
                bateriaCar = 100; // Como hemos repostado, la volvemos a poner al máximo
                */
            }
        }

        // Terminar sesión
        endSession();
    }

    /**
     * Le manda al servidor el comando con el movimiento del coche
     *
     * @author Andrés Molina López
     * @param nextMove indica cual es el string que se va a mandar al servidor
     */
    private void makeMove(String nextMove) {
        boolean resultadoMovimiento = sendCommand(nextMove);
        cerebro.refreshMemory(resultadoMovimiento, nextMove);
    }

    /**
     * Recarga la bateria del coche
     *
     * @author Andrés Molina López
     */
    private void refuel(){
        sendCommand(Mensajes.AGENT_COM_ACCION_REFUEL);
        cerebro.refreshBatery();
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

    /**
     * Iniciar el procesamiento de la percepción
     *
     * @author Diego Iáñez Ávila
     */
    private void processPerception(){
        try {
            // Recibimos los mensajes del servidor en orden
            ArrayList<JsonObject> messages = new ArrayList<>();

            for (int i = 0; i < numSensores; ++i) {
                JsonObject msg = receiveJson();
                messages.add(msg);
            }

            cerebro.processPerception(messages);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
