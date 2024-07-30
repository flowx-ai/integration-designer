package ai.flowx.integration.exceptions;


public class ExceptionMessages {

    public static final String SYSTEM_CODE_EXISTS = "A different system exists with this code.";
    public static final String SYSTEM_NOT_FOUND = "System not found.";
    public static final String SYSTEM_NOT_UPDATED = "System not updated.";
    public static final String SYSTEM_VARIABLE_KEY_EXISTS = "A different variable exists with this key.";
    public static final String VARIABLE_NOT_FOUND = "Variable not found.";
    public static final String ENDPOINT_NOT_FOUND = "Endpoint not found.";
    public static final String ENDPOINT_NOT_UPDATED = "Endpoint not updated.";
    public static final String PARAMETER_NOT_FOUND = "Endpoint parameter not found.";
    public static final String ID_NOT_NULL = "Id must be null.";
    public static final String RESPONSE_NOT_FOUND = "Endpoint response not found.";
    public static final String WORKFLOW_NOT_FOUND = "Workflow not found.";
    public static final String WORKFLOW_NAME_REQUIRED = "Workflow name is required.";
    public static final String WORKFLOW_NAME_EXISTS = "A different workflow exists with this name.";
    public static final String INVALID_ID = "Invalid id";

    ExceptionMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
