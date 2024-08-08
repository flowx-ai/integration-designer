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
public class WorkflowNodeForkRunner implements WorkflowNodeRunner{
    private final ObjectMapper objectMapper;

    @Override
    public NodeRunResponseDTO runNode(WorkflowNode workflowNode, Map<String, Object> input) {
        return NodeRunResponseDTO.builder()
                .output(objectMapper.valueToTree(Map.of("test", "fork")))
                .passedConditionId("1")
                .build();
    }

    @Override
    public WorkflowNodeType getSupportedType() {
        return WorkflowNodeType.FORK;
    }
}
