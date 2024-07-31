package ai.flowx.integration.repository;


import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.dto.WorkflowNodePositionDTO;

import java.util.Set;

public interface CustomWorkflowNodeRepository {
    void bulkUpdateLayoutOptions(Set<WorkflowNodePositionDTO> workflowNodePositionDTOSet);

    void updateGeneralNode(WorkflowNode node);
}
