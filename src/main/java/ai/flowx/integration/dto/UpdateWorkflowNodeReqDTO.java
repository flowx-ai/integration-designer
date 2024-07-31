package ai.flowx.integration.dto;

import ai.flowx.integration.domain.enums.ScriptLanguage;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateWorkflowNodeReqDTO {
    private String id;
    private String name;
    private Map<String, Object> layoutOptions;
    private List<ConditionDTO> conditions;
    private ScriptLanguage language;
    private String script;
    private String inputBody;
    private String outputBody;
    private String endpointFlowxUuid;
    private List<NodeVariableDTO> variables;
    private String payload;
}
