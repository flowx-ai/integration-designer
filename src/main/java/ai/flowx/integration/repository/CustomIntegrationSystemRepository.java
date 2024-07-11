package ai.flowx.integration.repository;

import ai.flowx.integration.domain.Authorization;
import ai.flowx.integration.domain.IntegrationSystem;
import ai.flowx.integration.domain.Variable;

public interface CustomIntegrationSystemRepository {
    Variable saveVariable(String id, Variable variable);

    IntegrationSystem updateGeneral(IntegrationSystem entity);

    Authorization updateAuthorization(String id, Authorization entity);

    Variable updateVariable(String id, Variable variable);

    void deleteVariable(String systemId, String variableId);
}
