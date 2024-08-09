package ai.flowx.integration.advancing.worker.repository;

import ai.flowx.integration.advancing.config.AdvancingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class WorkerRepositoryFactory {
    private static Map<String, BiFunction<AdvancingProperties, ObjectMapper, WorkerRepository>> INIT_FUNCTIONS =
            Map.of("oracle", OracleWorkerRepository::new,
                    "postgresql", PostgresWorkerRepository::new);

    public static WorkerRepository createWorkerRepositoryInstance(String dbType, AdvancingProperties advancingProperties, ObjectMapper objectMapper) {
        return Optional.ofNullable(INIT_FUNCTIONS.get(dbType))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported db type: " + dbType))
                .apply(advancingProperties, objectMapper);
    }
}
