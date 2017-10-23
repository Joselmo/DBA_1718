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

    /**
     * Constructor
     *
     * @author Diego Iáñez Ávila, David Vargas Carrillo, Andrés Molina López
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

        sendMessage(jsonLogin.toString());

        // Recibir y guardar la contraseña
        try {
            JsonObject answer = receiveJson();
            password = answer.getString("result", null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cuerpo del agente
     *
     * @author Diego Iáñez Ávila
     */
    @Override
    public void execute(){
        // Cuando esté implementado de verdad, la condición de salida del bucle no será esta
        // y por lo tanto no se comprobará dos veces como ahora.
        while (!reachedGoal) {
            processPerception();

            if (!reachedGoal)
                sendCommand("moveSW");
        }

        // Terminar sesión
        endSession();
    }

    /**
     * Recibe y procesa la percepción del agente
     *
     * @author Diego Iáñez Ávila
     */
    private void processPerception(){
        try {
            JsonObject jsonRadar = receiveJson();
            JsonArray radar = jsonRadar.get("radar").asArray();
            int pos = radar.get(12).asInt();

            if (pos == 2)
                reachedGoal = true;

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
            System.out.println("Traza guardada");

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
     * Envia un comando al controlador
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
