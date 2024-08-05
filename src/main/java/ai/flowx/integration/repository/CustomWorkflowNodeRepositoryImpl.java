package ai.flowx.integration.repository;

import ai.flowx.commons.definitions.audit.domain.AuditorDetails;
import ai.flowx.commons.errors.InternalServerErrorException;
import ai.flowx.integration.config.audit.AuditUtils;
import ai.flowx.integration.domain.Sequence;
import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.dto.WorkflowNodePositionDTO;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ai.flowx.integration.exceptions.ExceptionMessages.NODES_NOT_UPDATED;
import static ai.flowx.integration.exceptions.ExceptionMessages.NODE_NOT_UPDATED;
import static ai.flowx.integration.repository.utils.WorkflowNodeFieldNames.*;

@Repository
@RequiredArgsConstructor
public class CustomWorkflowNodeRepositoryImpl implements CustomWorkflowNodeRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public void bulkUpdateLayoutOptions(Set<WorkflowNodePositionDTO> workflowNodePositionDTOSet) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, WorkflowNode.class);
        Date modifiedDate = new Date();
        AuditorDetails auditorDetails = AuditUtils.getAuditorDetails();
        workflowNodePositionDTOSet.forEach(nodePos -> {
            Query query = new Query(Criteria.where(ID).is(nodePos.getWorkflowNodeId()));
            Update update = new Update();
            update.set(LAYOUT_OPTIONS, nodePos.getLayoutOptions());
            update.set(MODIFIED_DATE, modifiedDate);
            update.set(MODIFIED_BY, auditorDetails);
            bulkOps.updateOne(query, update);
        });
        executeBulkUpdate(bulkOps);
    }

    @Override
    public void updateGeneralNode(WorkflowNode node) {
        Query query = new Query(Criteria.where(ID).is(node.getId()));
        Update update = new Update();
        update.set(NAME, node.getName());
        update.set(LAYOUT_OPTIONS, node.getLayoutOptions());
        update.set(CONDITIONS, node.getConditions());
        update.set(LANGUAGE, node.getLanguage());
        update.set(SCRIPT, node.getScript());
        update.set(INPUT_BODY, node.getInputBody());
        update.set(OUTPUT_BODY, node.getOutputBody());
        update.set(ENDPOINT_FLOWX_UUID, node.getEndpointFlowxUuid());
        update.set(INTEGRATION_SYSTEM_FLOWX_UUID, node.getIntegrationSystemFlowxUuid());
        update.set(VARIABLES, node.getVariables());
        update.set(PAYLOAD, node.getPayload());
        executeUpdate(query, update);
    }

    @Override
    public void addSequence(String workflowNodeId, Sequence sequence) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(workflowNodeId));
        Update update = new Update();
        update.push(SEQUENCES, sequence);
        executeUpdate(updateQuery, update);
    }

    @Override
    public List<String> findSequenceIdsByWorkflowNodeFlowxUuid(String workflowId, String workflowNodeFlowxUuid) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(WORKFLOW_ID).is(workflowId).and(SEQUENCES_TARGET).is(workflowNodeFlowxUuid)),
                Aggregation.unwind("$" + SEQUENCES),
                Aggregation.match(Criteria.where(SEQUENCES_TARGET).is(workflowNodeFlowxUuid)),
                Aggregation.project(SEQUENCES_ID)
        );

        List<Document> mappedResults = mongoTemplate.aggregate(aggregation, WorkflowNode.class, Document.class).getMappedResults();
        return mappedResults.stream().map(s -> s.getString("_" + ID)).collect(Collectors.toList());
    }

    @Override
    public void deleteSequences(String workflowId, List<String> deletedSequences) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(WORKFLOW_ID).is(workflowId));

        Query arrayQuery = new Query();
        arrayQuery.addCriteria(Criteria.where("_" + ID).in(deletedSequences));

        Update update = new Update().pull(SEQUENCES, arrayQuery);
        mongoTemplate.updateMulti(updateQuery, update, WorkflowNode.class);
    }

    private void executeBulkUpdate(BulkOperations bulkOps) {
        BulkWriteResult bulkOpsResult = bulkOps.execute();
        if (bulkOpsResult.getModifiedCount() == 0) {
            throw new InternalServerErrorException(NODES_NOT_UPDATED, WorkflowNode.class.getName(),
                    BadRequestErrorType.NODES_NOT_UPDATED);
        }
    }

    private void executeUpdate(Query updateQuery, Update update) {
        update.set(MODIFIED_DATE, new Date());
        update.set(MODIFIED_BY, AuditUtils.getAuditorDetails());

        UpdateResult updateResult = mongoTemplate.updateFirst(updateQuery, update, WorkflowNode.class);
        if (updateResult.getModifiedCount() == 0) {
            throw new InternalServerErrorException(NODE_NOT_UPDATED, WorkflowNode.class.getName(),
                    BadRequestErrorType.NODE_NOT_UPDATED);
        }
    }
}
