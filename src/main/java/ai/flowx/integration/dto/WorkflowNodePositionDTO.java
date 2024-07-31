package ai.flowx.integration.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WorkflowNodePositionDTO {
    private String workflowNodeId;
    private Map<String, Object> layoutOptions;
}
