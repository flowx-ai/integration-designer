package ai.flowx.integration.dto;

import ai.flowx.integration.domain.enums.WorkflowNodeType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateWorkflowNodeReqDTO {
    private String name;
    private WorkflowNodeType type;
    private String workflowId;
    private Map<String, Object> layoutOptions;
}
