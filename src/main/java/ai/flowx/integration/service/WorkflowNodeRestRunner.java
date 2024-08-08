package ai.flowx.integration.service;

import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.StatusType;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.dto.NodeRunResponseDTO;
import ai.flowx.integration.dto.TestEndpointResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkflowNodeRestRunner implements WorkflowNodeRunner{

    private final ObjectMapper objectMapper;
    private final TestRunEndpointService testRunEndpointService;
    private final EndpointService endpointService;

    @Override
    public NodeRunResponseDTO runNode(WorkflowNode workflowNode, Map<String, Object> input) {
        TestEndpointResponseDTO testEndpointResponseDTO = testRunEndpointService.executeRequestForNodeRun(workflowNode, input).block();

        if(testEndpointResponseDTO.getCode().is2xxSuccessful()) {
            return NodeRunResponseDTO.builder()
                    .output(testEndpointResponseDTO.getBody())
                    .status(StatusType.SUCCESS)
                    .build();
        } else {
            return NodeRunResponseDTO.builder()
                    .output(testEndpointResponseDTO.getBody())
                    .status(StatusType.FAIL)
                    .build();
        }
    }

    @Override
    public WorkflowNodeType getSupportedType() {
        return WorkflowNodeType.REST;
    }
}
