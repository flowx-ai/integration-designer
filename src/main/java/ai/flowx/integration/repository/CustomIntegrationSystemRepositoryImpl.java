package ai.flowx.integration.repository;

import ai.flowx.commons.errors.InternalServerErrorException;
import ai.flowx.integration.config.audit.AuditUtils;
import ai.flowx.integration.domain.Authorization;
import ai.flowx.integration.domain.IntegrationSystem;
import ai.flowx.integration.domain.Variable;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;

import static ai.flowx.integration.exceptions.ExceptionMessages.SYSTEM_NOT_UPDATED;
import static ai.flowx.integration.repository.IntegrationSystemFieldNames.*;

@RequiredArgsConstructor
@Repository
public class CustomIntegrationSystemRepositoryImpl implements CustomIntegrationSystemRepository {
    private final MongoTemplate mongoTemplate;

    public Variable saveVariable(String id, Variable variable) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(id));
        Update update = new Update();
        update.push(VARIABLES, variable);
        executeUpdate(updateQuery, update);
        return variable;
    }

    public IntegrationSystem updateGeneral(IntegrationSystem entity) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(entity.getId()));
        Update update = new Update();
        update.set(NAME, entity.getName());
        update.set(CODE, entity.getCode());
        update.set(BASE_URL, entity.getBaseUrl());
        update.set(DESCRIPTION, entity.getDescription());

        executeUpdate(updateQuery, update);
        return entity;
    }

    public Authorization updateAuthorization(String id, Authorization entity) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(id));
        Update update = new Update();
        update.set(AUTHORIZATION, entity);

        executeUpdate(updateQuery, update);
        return entity;
    }

    public Variable updateVariable(String id, Variable variable) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(id));
        updateQuery.addCriteria(Criteria.where(VARIABLE_ID).is(variable.getId()));
        Update update = new Update();
        update.set(VARIABLES_$, variable);
        executeUpdate(updateQuery, update);
        return variable;
    }

    public void deleteVariable(String systemId, String variableId) {
        Query updateQuery = new Query();
        updateQuery.addCriteria(Criteria.where(ID).is(systemId));

        Query arrayQuery = new Query();
        arrayQuery.addCriteria(Criteria.where(ID).is(variableId));

        Update update = new Update().pull(VARIABLES, arrayQuery);
        executeUpdate(updateQuery, update);
    }

    private void executeUpdate(Query updateQuery, Update update) {
        update.set(MODIFIED_DATE, new Date());
        update.set(MODIFIED_BY, AuditUtils.getAuditorDetails());

        UpdateResult updateResult = mongoTemplate.updateFirst(updateQuery, update, IntegrationSystem.class);
        if (updateResult.getModifiedCount() == 0) {
            throw new InternalServerErrorException(SYSTEM_NOT_UPDATED, CustomIntegrationSystemRepositoryImpl.class.getName(),
                    BadRequestErrorType.SYSTEM_NOT_UPDATED);
        }
    }

}
