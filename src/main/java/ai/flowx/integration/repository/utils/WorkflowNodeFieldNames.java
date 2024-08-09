package ai.flowx.integration.repository.utils;

public class WorkflowNodeFieldNames {

    public static final String COLLECTION_NAME = "workflow_nodes";

    public static final String ID = "id";
    public static final String MODIFIED_DATE = "modifiedDate";
    public static final String MODIFIED_BY = "modifiedBy";

    public static final String LAYOUT_OPTIONS = "layoutOptions";
    public static final String NAME = "name";
    public static final String CONDITIONS = "conditions";
    public static final String LANGUAGE = "language";
    public static final String SCRIPT = "script";
    public static final String INPUT_BODY = "inputBody";
    public static final String OUTPUT_BODY = "outputBody";
    public static final String ENDPOINT_FLOWX_UUID = "endpointFlowxUuid";
    public static final String INTEGRATION_SYSTEM_FLOWX_UUID = "integrationSystemFlowxUuid";
    public static final String VARIABLES = "variables";
    public static final String PAYLOAD = "payload";
    public static final String WORKFLOW_ID = "workflowId";

    public static final String SEQUENCES = "outgoingSequences";
    public static final String SEQUENCES_TARGET = "outgoingSequences.targetNodeFlowxUuid";
    public static final String SEQUENCES_ID = "outgoingSequences._id";
    public static final String SEQUENCES_$ = "outgoingSequences.$";
    public static final String CONDITION_ID = "conditionId";

    WorkflowNodeFieldNames() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
