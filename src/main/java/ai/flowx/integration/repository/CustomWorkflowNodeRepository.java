package ai.flowx.integration.repository;


import ai.flowx.integration.domain.Sequence;
import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.dto.WorkflowNodePositionDTO;

import java.util.List;
import java.util.Set;

public interface CustomWorkflowNodeRepository {
    void bulkUpdateLayoutOptions(Set<WorkflowNodePositionDTO> workflowNodePositionDTOSet);

    void updateGeneralNode(WorkflowNode node, List<String> deletedConditionsIds);

    void addSequence(String workflowNodeId, Sequence sequence);

    List<String> findSequenceIdsByWorkflowNodeFlowxUuid(String flowxUuid, String uuid);

    void deleteSequences(String workflowId, List<String> deletedSequences);
}
