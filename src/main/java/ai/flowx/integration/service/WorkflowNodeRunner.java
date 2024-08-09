package ai.flowx.integration.service;

import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.dto.NodeRunResponseDTO;

import java.util.Map;

public interface WorkflowNodeRunner {

    NodeRunResponseDTO runNode(WorkflowNode workflowNode, Map<String, Object> input);
    WorkflowNodeType getSupportedType();
}
