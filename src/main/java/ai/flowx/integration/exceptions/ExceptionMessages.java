package ai.flowx.integration.exceptions;


public class ExceptionMessages {

    public static final String SYSTEM_CODE_EXISTS = "A different system exists with this code.";
    public static final String SYSTEM_NOT_FOUND = "System not found.";
    public static final String SYSTEM_NOT_UPDATED = "System not updated.";
    public static final String SYSTEM_VARIABLE_KEY_EXISTS = "A different variable exists with this key.";
    public static final String VARIABLE_NOT_FOUND = "Variable not found.";

    ExceptionMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
