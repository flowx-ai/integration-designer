package ai.flowx.integration.repository;

import ai.flowx.integration.domain.WorkflowNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface WorkflowNodeRepository extends MongoRepository<WorkflowNode, String>, CustomWorkflowNodeRepository {

    List<WorkflowNode> findByIdOrFlowxUuid(String id, String flowxUuid);
    Optional<WorkflowNode> findWorkflowNodeByFlowxUuid(String flowxUuid);

    void deleteAllByWorkflowIdAndOutgoingSequencesIdIn(String workflowId, Collection<String> sequencesIds);
}
