package ai.flowx.integration.repository;

import ai.flowx.integration.domain.Endpoint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EndpointRepository extends MongoRepository<Endpoint, String>, EndpointCustomRepository {

    List<Endpoint> findAllBySystemId(String systemId);
}
