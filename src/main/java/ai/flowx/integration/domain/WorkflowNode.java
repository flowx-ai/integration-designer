package ai.flowx.integration.domain;

import ai.flowx.commons.definitions.audit.domain.BaseAuditableEntity;
import ai.flowx.integration.domain.enums.ScriptLanguage;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "workflow_nodes")
public class WorkflowNode extends BaseAuditableEntity implements Serializable {
    @Id
    private String id;
    private String flowxUuid;
    private WorkflowNodeType type;
    private String name;
    private Map<String, Object> layoutOptions;
    private List<Condition> conditions;
    private List<Sequence> outgoingSequences;
    private ScriptLanguage language;
    private String script;
    private String inputBody;
    private String outputBody;
    private String endpointFlowxUuid;
    private List<NodeVariable> variables;
    private String payload;
    private String workflowId;
}

