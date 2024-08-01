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
    public static final String WORKFLOW_NOT_FOUND = "Workflow not found";
    public static final String WORKFLOW_NAME_REQUIRED = "Workflow name is required.";
    public static final String WORKFLOW_NAME_EXISTS = "A different workflow exists with this name.";
    public static final String INVALID_ID = "Invalid id";
    public static final String INVALID_NODE_TYPE = "Node type is invalid.";
    public static final String WORKFLOW_NODE_NAME_REQUIRED = "Workflow node name is required.";
    public static final String NODES_NOT_UPDATED = "Workflow nodes not updated.";
    public static final String NODE_NOT_UPDATED = "Workflow node not updated.";
    public static final String WORKFLOW_NODE_NOT_FOUND = "Workflow node not found.";
    public static final String WORKFLOW_NOT_UPDATED = "Workflow not updated.";
    public static final String WORKFLOW_NODE_SEQUENCE_NOT_VALID = "Workflow node sequence is not valid!";
    public static final String WORKFLOW_NODE_SEQUENCE_NOT_VALID_ONE_SEQUENCE_ALLOWED = "Node already has sequence!";
    public static final String WORKFLOW_NODE_SEQUENCE_NOT_VALID_END_NODE_NOT_ALLOWED = "End node not allowed to have sequence!";
    public static final String WORKFLOW_NODE_SEQUENCE_NOT_VALID_REST_NODE_HAS_STATUS_OUTPUT = "Rest node has that status output type in sequences!";
    public static final String WORKFLOW_NODE_SEQUENCE_NOT_VALID_FORK_CONDITION_ID_NOT_FOUND = "Fork condition id does not exist!";
    public static final String WORKFLOW_NODE_SEQUENCE_NOT_VALID_SRC_TARGET_BOTH_START_END = "One of the nodes must be different type from START/END!";
    public static final String WORKFLOW_NODE_SEQUENCE_NOT_VALID_START_TO_END = "START node cannot have sequence to END node!";

    ExceptionMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
