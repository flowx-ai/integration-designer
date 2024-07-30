package ai.flowx.integration.repository;

import ai.flowx.integration.domain.Workflow;
import ai.flowx.integration.domain.WorkflowDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static ai.flowx.integration.repository.WorkflowFieldNames.ID;

@Repository
@RequiredArgsConstructor
public class CustomWorkflowRepositoryImpl implements CustomWorkflowRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<WorkflowDefinition> getWorkflowWithNodes(String workflowId) {

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from(WorkflowFieldNames.COLLECTION_NAME)
                .localField("id")
                .foreignField("workflowId")
                .as("nodes");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(new Criteria(ID).is(workflowId)), lookupOperation);

        AggregationResults<WorkflowDefinition> results = mongoTemplate.aggregate(aggregation, Workflow.class, WorkflowDefinition.class);
        return Optional.ofNullable(results.getMappedResults()).flatMap(r -> r.stream().findFirst());
    }
}
