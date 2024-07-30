package ai.flowx.integration.repository;

import ai.flowx.integration.domain.Workflow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepository extends MongoRepository<Workflow, String>, CustomWorkflowRepository {

    boolean existsByName(String name);
}
