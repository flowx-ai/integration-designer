package ai.flowx.integration.repository;

import ai.flowx.integration.domain.WorkflowNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowNodeRepository extends MongoRepository<WorkflowNode, String>, CustomWorkflowNodeRepository {

    List<WorkflowNode> findByIdOrFlowxUuid(String id, String flowxUuid);
}
