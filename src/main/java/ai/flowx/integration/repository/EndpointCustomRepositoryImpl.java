package ai.flowx.integration.repository;


import ai.flowx.commons.errors.InternalServerErrorException;
import ai.flowx.integration.config.audit.AuditUtils;
import ai.flowx.integration.domain.*;
import ai.flowx.integration.dto.SystemEndpointSummaryDTO;
import ai.flowx.integration.dto.enums.ParamType;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import ai.flowx.integration.repository.utils.IntegrationSystemFieldNames;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ai.flowx.integration.exceptions.ExceptionMessages.ENDPOINT_NOT_UPDATED;
import static ai.flowx.integration.repository.utils.EndpointFieldNames.*;
import static ai.flowx.integration.repository.utils.EndpointFieldNames.ID;
import static ai.flowx.integration.repository.utils.EndpointFieldNames.MODIFIED_BY;
import static ai.flowx.integration.repository.utils.EndpointFieldNames.MODIFIED_DATE;
import static ai.flowx.integration.repository.utils.EndpointFieldNames.NAME;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.addFields;

@Repository
@RequiredArgsConstructor
public class EndpointCustomRepositoryImpl implements EndpointCustomRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Endpoint updateGeneral(Endpoint entity) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(entity.getId()));
        Update update = new Update();
        update.set(NAME, entity.getName());
        update.set(DESCRIPTION, entity.getDescription());
        update.set(URL, entity.getUrl());
        update.set(HTTP_METHOD, entity.getHttpMethod());
        update.set(PAYLOAD, entity.getPayload());

        executeUpdate(updateQuery, update);
        return entity;
    }

    @Override
    public EndpointParam saveParam(String endpointId, ParamType type, EndpointParam endpointParam) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(endpointId));
        Update update = new Update();
        update.push(PARAM_NAMES_BY_PARAM_TYPE.get(type), endpointParam);
        executeUpdate(updateQuery, update);
        return endpointParam;
    }

    @Override
    public EndpointParam updateParam(String endpointId, ParamType type, EndpointParam endpointParam) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(endpointId));
        updateQuery.addCriteria(Criteria.where(PARAM_IDS_BY_PARAM_TYPE.get(type)).is(endpointParam.getId()));
        Update update = new Update();
        update.set(PARAM_NAMES_$_BY_PARAM_TYPE.get(type), endpointParam);
        executeUpdate(updateQuery, update);
        return endpointParam;
    }

    @Override
    public void deleteParam(String endpointId, ParamType type, String paramId) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(endpointId));

        Query arrayQuery = new Query();
        arrayQuery.addCriteria(Criteria.where(ID).is(paramId));

        Update update = new Update().pull(PARAM_NAMES_BY_PARAM_TYPE.get(type), arrayQuery);
        executeUpdate(updateQuery, update);
    }

    @Override
    public EndpointResponse saveResponse(String endpointId, EndpointResponse endpointResponse) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(endpointId));
        Update update = new Update();
        update.push(RESPONSES, endpointResponse);
        executeUpdate(updateQuery, update);
        return endpointResponse;
    }

    @Override
    public EndpointResponse updateResponse(String endpointId, EndpointResponse endpointResponse) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(endpointId));
        updateQuery.addCriteria(Criteria.where(RESPONSES_ID).is(endpointResponse.getId()));
        Update update = new Update();
        update.set(RESPONSES_$, endpointResponse);
        executeUpdate(updateQuery, update);
        return endpointResponse;
    }

    @Override
    public void deleteResponse(String endpointId, String endpointResponseId) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(endpointId));

        Query arrayQuery = new Query();
        arrayQuery.addCriteria(Criteria.where(ID).is(endpointResponseId));

        Update update = new Update().pull(RESPONSES, arrayQuery);
        executeUpdate(updateQuery, update);
    }

    @Override
    public Optional<EndpointWithSystem> getEndpointWithSystem(String endpointId) {
        return getEndpointWithSystem(new Criteria(ID).is(endpointId));
    }

    @Override
    public Optional<EndpointWithSystem> getEndpointWithSystemUsingUuid(String endpointFlowxUuid) {
        return getEndpointWithSystem(new Criteria(FLOWX_UUID).is(endpointFlowxUuid));
    }

    private Optional<EndpointWithSystem> getEndpointWithSystem(Criteria matchCriteria) {
        AggregationOperation addFields = addFields()
                .addFieldWithValue("convertedSystemId", ConvertOperators.ToObjectId.toObjectId("$systemId"))
                .build();
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from(IntegrationSystemFieldNames.COLLECTION_NAME)
                .localField("convertedSystemId")
                .foreignField("_id")
                .as("system");


        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(matchCriteria), addFields,
                lookupOperation, Aggregation.unwind("system"));

        AggregationResults<EndpointWithSystem> results = mongoTemplate.aggregate(aggregation, Endpoint.class, EndpointWithSystem.class);
        return Optional.ofNullable(results.getMappedResults()).flatMap(r -> r.stream().findFirst());
    }

    @Override
    public List<EndpointWithSystem> getEndpointsWithSystemCodeByFlowxUuids(Set<String> flowxUuids) {
        AggregationOperation addFields = addFields()
                .addFieldWithValue("convertedSystemId", ConvertOperators.ToObjectId.toObjectId("$systemId"))
                .build();
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from(IntegrationSystemFieldNames.COLLECTION_NAME)
                .localField("convertedSystemId")
                .foreignField("_id")
                .as("system");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(new Criteria(FLOWX_UUID).in(flowxUuids)), addFields,
                lookupOperation, Aggregation.unwind("system"),
                Aggregation.project(IntegrationSystemFieldNames.ID, FLOWX_UUID, NAME, URL, HTTP_METHOD, PAYLOAD, HEADERS, QUERY_PARAMETERS, PATH_PARAMETERS)
                        .and("system.code").as("system.code"));

        AggregationResults<EndpointWithSystem> results = mongoTemplate.aggregate(aggregation, Endpoint.class, EndpointWithSystem.class);
        return results.getMappedResults();
    }

    @Override
    public Optional<EndpointMetadata> getEndpointMetadata(String endpointFlowxUuid) {
        AggregationOperation addFields = addFields()
                .addFieldWithValue("convertedSystemId", ConvertOperators.ToObjectId.toObjectId("$systemId"))
                .build();
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from(IntegrationSystemFieldNames.COLLECTION_NAME)
                .localField("convertedSystemId")
                .foreignField("_id")
                .as("system");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(new Criteria(FLOWX_UUID).is(endpointFlowxUuid)), addFields,
                lookupOperation, Aggregation.unwind("system"), Aggregation.project(ID, FLOWX_UUID)
                        .and("system.flowxUuid").as("integrationSystemFlowxUuid"));

        AggregationResults<EndpointMetadata> results = mongoTemplate.aggregate(aggregation, Endpoint.class, EndpointMetadata.class);
        return Optional.ofNullable(results.getMappedResults()).flatMap(r -> r.stream().findFirst());
    }

    @Override
    public SystemEndpointSummaryDTO updateNameMethodAndDescription(SystemEndpointSummaryDTO endpointSummaryDTO) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(endpointSummaryDTO.getId()));
        Update update = new Update();
        update.set(NAME, endpointSummaryDTO.getName());
        update.set(DESCRIPTION, endpointSummaryDTO.getDescription());
        update.set(HTTP_METHOD, endpointSummaryDTO.getHttpMethod());

        executeUpdate(updateQuery, update);
        return endpointSummaryDTO;
    }

    private void executeUpdate(Query updateQuery, Update update) {
        update.set(MODIFIED_DATE, new Date());
        update.set(MODIFIED_BY, AuditUtils.getAuditorDetails());

        UpdateResult updateResult = mongoTemplate.updateFirst(updateQuery, update, Endpoint.class);
        if (updateResult.getModifiedCount() == 0) {
            throw new InternalServerErrorException(ENDPOINT_NOT_UPDATED, EndpointCustomRepository.class.getName(),
                    BadRequestErrorType.ENDPOINT_NOT_UPDATED);
        }
    }
}
