package ai.flowx.integration.repository;


import ai.flowx.commons.errors.InternalServerErrorException;
import ai.flowx.integration.config.audit.AuditUtils;
import ai.flowx.integration.domain.Endpoint;
import ai.flowx.integration.domain.EndpointParam;
import ai.flowx.integration.domain.EndpointResponse;
import ai.flowx.integration.domain.EndpointWithSystemSummary;
import ai.flowx.integration.dto.SystemEndpointSummaryDTO;
import ai.flowx.integration.dto.enums.ParamType;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

import static ai.flowx.integration.exceptions.ExceptionMessages.ENDPOINT_NOT_UPDATED;
import static ai.flowx.integration.repository.EndpointFieldNames.*;
import static ai.flowx.integration.repository.EndpointFieldNames.ID;
import static ai.flowx.integration.repository.EndpointFieldNames.MODIFIED_BY;
import static ai.flowx.integration.repository.EndpointFieldNames.MODIFIED_DATE;
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
    public Optional<EndpointWithSystemSummary> getEndpointWithSystemSummary(String endpointId) {
        AggregationOperation addFields = addFields()
                .addFieldWithValue("convertedSystemId", ConvertOperators.ToObjectId.toObjectId("$systemId"))
                .build();
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from(IntegrationSystemFieldNames.COLLECTION_NAME)
                .localField("convertedSystemId")
                .foreignField("_id")
                .as("system");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(new Criteria(ID).is(endpointId)), addFields,
                lookupOperation, Aggregation.unwind("system"));

        AggregationResults<EndpointWithSystemSummary> results = mongoTemplate.aggregate(aggregation, Endpoint.class, EndpointWithSystemSummary.class);
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
