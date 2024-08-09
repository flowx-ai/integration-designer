package ai.flowx.integration.dto;

import ai.flowx.commons.definitions.audit.dtos.BaseAuditableEntityDTO;
import ai.flowx.integration.domain.enums.ScriptLanguage;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@SuperBuilder
@Getter
@Setter
public class WorkflowNodeDTO extends BaseAuditableEntityDTO {
    @Id
    private String id;
    private String flowxUuid;
    private WorkflowNodeType type;
    private String name;
    private Map<String, Object> layoutOptions;
    private List<ConditionDTO> conditions;
    private List<SequenceDTO> outgoingSequences;
    private ScriptLanguage language;
    private String script;
    private String inputBody;
    private String outputBody;
    private String endpointFlowxUuid;
    private List<NodeVariableDTO> variables;
    private String payload;
    private String workflowId;
}

