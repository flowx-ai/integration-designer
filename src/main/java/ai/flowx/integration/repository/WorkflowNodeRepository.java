package ai.flowx.integration.repository;

import ai.flowx.integration.domain.WorkflowNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowNodeRepository extends MongoRepository<WorkflowNode, String>, CustomWorkflowNodeRepository {

}
