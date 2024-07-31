package ai.flowx.integration.repository;


import ai.flowx.integration.domain.WorkflowDefinition;
import ai.flowx.integration.domain.WorkflowWithSystemsCodes;

import java.util.List;
import java.util.Optional;

public interface CustomWorkflowRepository {
    Optional<WorkflowDefinition> getWorkflowWithNodes(String workflowId);

    void updateSystemCounters(String workflowId, String systemFlowxUuidToRemove, String systemFlowxUuidToAdd);

    List<WorkflowWithSystemsCodes> getAllWorkflows();
}
