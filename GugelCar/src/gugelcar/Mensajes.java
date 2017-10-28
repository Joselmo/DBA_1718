package gugelcar;

/**
 * Enum que contiene los mensajes de la aplicación
 * incluyendo los mensajes del agente.
 *
 * @author Jose Luis Martínez Ortiz
 *
 *
 */
public enum Mensajes {

    INSTANCE;

    // Configuración del servidor
    static final String AGENT_HOST = "Girtab";
    static final String AGENT_USER = "Eridano";
    static final String AGENT_PASS = "Esquivel";


    // MENSAJES DEL AGENTE
    static final String AGENT_COM_OK = "OK";
    static final String AGENT_COM_BADMESSAGE = "BAD_MESSAGE";

    static final String AGENT_COM_LOGOUT = "logout";
    static final String AGENT_COM_LOGIN = "login";

    static final String AGENT_COM_RESULT = "result";

    static final String AGENT_COM_COMMAND = "command";
    static final String AGENT_COM_KEY = "key";
    static final String AGENT_COM_WORLD = "world";
    static final String AGENT_COM_SENSOR_SCANNER = "scanner";
    static final String AGENT_COM_SENSOR_RADAR = "radar";

    static final String AGENT_COM_TRACE = "trace";

    // ACCIONES DEL COCHE
    static final String AGENT_COM_ACCION_REFUEL = "refuel";
    static final String AGENT_COM_ACCION_MV_NW = "moveNW";
    static final String AGENT_COM_ACCION_MV_N = "moveN";
    static final String AGENT_COM_ACCION_MV_NE = "moveNE";
    static final String AGENT_COM_ACCION_MV_W = "moveW";
    static final String AGENT_COM_ACCION_MV_E = "moveE";
    static final String AGENT_COM_ACCION_MV_SW = "moveSW";
    static final String AGENT_COM_ACCION_MV_S = "moveS";
    static final String AGENT_COM_ACCION_MV_SE = "moveSE";



    // MENSAJES DE GUGEL CAR






}