package ai.flowx.integration.service;

import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.dto.NodeRunResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;

public interface WorkflowNodeRunner {

    NodeRunResponseDTO runNode(WorkflowNode workflowNode, JsonNode input);
    WorkflowNodeType getSupportedType();
}
