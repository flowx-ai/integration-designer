package ai.flowx.integration.repository;

import ai.flowx.commons.errors.InternalServerErrorException;
import ai.flowx.integration.domain.*;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import ai.flowx.integration.repository.utils.IntegrationSystemFieldNames;
import ai.flowx.integration.repository.utils.WorkflowFieldNames;
import ai.flowx.integration.repository.utils.WorkflowNodeFieldNames;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static ai.flowx.integration.exceptions.ExceptionMessages.WORKFLOW_NOT_UPDATED;
import static ai.flowx.integration.repository.utils.WorkflowFieldNames.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.addFields;

@Repository
@RequiredArgsConstructor
public class CustomWorkflowRepositoryImpl implements CustomWorkflowRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<WorkflowDefinition> getWorkflowWithNodes(String workflowId) {
        AggregationOperation addFields = addFields()
                .addFieldWithValue("convertedId", ConvertOperators.ToString.toString("$_id"))
                .build();
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from(WorkflowNodeFieldNames.COLLECTION_NAME)
                .localField("convertedId")
                .foreignField("workflowId")
                .as("nodes");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(new Criteria(ID).is(workflowId)), lookupOperation);

        AggregationResults<WorkflowDefinition> results = mongoTemplate.aggregate(aggregation, Workflow.class, WorkflowDefinition.class);
        return Optional.ofNullable(results.getMappedResults()).flatMap(r -> r.stream().findFirst());
    }

    @Override
    public void updateSystemCounters(String workflowId, String systemFlowxUuidToRemove, String systemFlowxUuidToAdd) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Workflow.class);

        if (StringUtils.hasText(systemFlowxUuidToAdd)) {
            Query query = new Query(Criteria.where("_id").is(workflowId)
                    .and("systems.integrationSystemFlowxUuid").ne(systemFlowxUuidToAdd));

            // Define the update to add the new element to the array
            Update update = new Update()
                    .addToSet("systems", new WorkflowIntegrationSystem(systemFlowxUuidToAdd, 0));

            bulkOps.updateOne(query, update);

            Query query1 = new Query(Criteria.where(ID).is(workflowId)
                    .and("systems.integrationSystemFlowxUuid").is(systemFlowxUuidToAdd));
            Update update1 = new Update().inc("systems.$.counter", 1);
            bulkOps.updateOne(query1, update1);
        }

        if (StringUtils.hasText(systemFlowxUuidToRemove)) {
            Query query = new Query(Criteria.where(ID).is(workflowId)
                    .and("systems.integrationSystemFlowxUuid").is(systemFlowxUuidToRemove));
            Update update = new Update().inc("systems.$.counter", -1);
            bulkOps.updateOne(query, update);
        }

        executeBulkUpdate(bulkOps);
    }

    @Override
    public List<WorkflowWithSystemsCodes> getAllWorkflows() {
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from(IntegrationSystemFieldNames.COLLECTION_NAME)
                .localField("systems.integrationSystemFlowxUuid")
                .foreignField("flowxUuid")
                .as("integrationsSystems");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(new Criteria()), lookupOperation,
                Aggregation.project(ID, FLOWX_UUID, NAME, DESCRIPTION, SYSTEMS)
                        .and("integrationsSystems.id").as("integrationsSystems.id")
                        .and("integrationsSystems.flowxUuid").as("integrationsSystems.flowxUuid")
                        .and("integrationsSystems.code").as("integrationsSystems.code"));

        AggregationResults<WorkflowWithSystemsCodes> results = mongoTemplate.aggregate(aggregation, Workflow.class, WorkflowWithSystemsCodes.class);
        return results.getMappedResults();
    }

    private void executeBulkUpdate(BulkOperations bulkOps) {
        BulkWriteResult bulkOpsResult = bulkOps.execute();
        if (bulkOpsResult.getModifiedCount() == 0) {
            throw new InternalServerErrorException(WORKFLOW_NOT_UPDATED, WorkflowNode.class.getName(),
                    BadRequestErrorType.WORKFLOW_NOT_UPDATED);

        }
    }
}
