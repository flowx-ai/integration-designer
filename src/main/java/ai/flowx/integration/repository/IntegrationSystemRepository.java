package ai.flowx.integration.repository;

import ai.flowx.integration.domain.IntegrationSystem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationSystemRepository extends MongoRepository<IntegrationSystem, String>, CustomIntegrationSystemRepository {
    @Query(value = "{}", fields = "{'authorization':0, 'variables':0}")
    List<IntegrationSystem> findAllSummaries();

    boolean existsByCode(String code);

    @Query(value = "{'id': ?0}", fields = "{'authorization':0, 'variables':0}")
    Optional<IntegrationSystem> findSummaryById(String id);

    @Query(value = "{'id': ?0}", fields = "{'id':1, 'variables':1}")
    Optional<IntegrationSystem> findVariablesBySystemId(String id);
}
