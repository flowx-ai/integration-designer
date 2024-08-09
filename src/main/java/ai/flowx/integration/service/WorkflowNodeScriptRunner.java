package ai.flowx.integration.service;

import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.dto.NodeRunResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowNodeScriptRunner implements WorkflowNodeRunner{

    private final ObjectMapper objectMapper;

    @Override
    public NodeRunResponseDTO runNode(WorkflowNode workflowNode, JsonNode input) {
        return NodeRunResponseDTO.builder()
                .output(objectMapper.valueToTree(Map.of("test", "script")))
                .build();
    }

    @Override
    public WorkflowNodeType getSupportedType() {
        return WorkflowNodeType.SCRIPT;
    }
}
