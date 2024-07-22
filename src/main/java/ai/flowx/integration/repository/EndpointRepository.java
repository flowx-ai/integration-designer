package ai.flowx.integration.repository;

import ai.flowx.integration.domain.Endpoint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EndpointRepository extends MongoRepository<Endpoint, String>, EndpointCustomRepository {

    @Query(value = "{'systemId': ?0}", fields = "{'id':1, 'name':1, 'httpMethod':1, 'description':1}")
    List<Endpoint> findAllSummariesBySystemId(String systemId);
}
