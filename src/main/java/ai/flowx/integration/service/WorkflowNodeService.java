package ai.flowx.integration.service;

import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.repository.WorkflowNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class WorkflowNodeService {
    private final WorkflowNodeRepository workflowNodeRepository;

    public void createStartNodeForWorkflow(String workflowId) {
        WorkflowNode node = new WorkflowNode();
        node.setName("start");
        node.setFlowxUuid(UUID.randomUUID().toString());
        node.setType(WorkflowNodeType.START);
        node.setWorkflowId(workflowId);
        node.setLayoutOptions(Map.of("x", 0, "y", 0));
        workflowNodeRepository.save(node);
    }
}
