package ai.flowx.integration.repository;


import ai.flowx.integration.domain.WorkflowDefinition;

import java.util.Optional;

public interface CustomWorkflowRepository {
    Optional<WorkflowDefinition> getWorkflowWithNodes(String workflowId);
}
