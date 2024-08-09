package ai.flowx.integration.advancing.repository.impl;

import ai.flowx.integration.advancing.dto.enums.AdvancingPartitionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@ConditionalOnExpression("'${spring.jpa.database}' == 'oracle'")
@Slf4j
@Repository
public class OracleAdvancingRepositoryImpl extends AdvancingRepositoryImpl {

    public OracleAdvancingRepositoryImpl(JdbcTemplate advancingJdbcTemplate, ObjectMapper objectMapper) {
        super(advancingJdbcTemplate, objectMapper);
    }

    public void updateHeartbeat(String currentPodName) {
        String sql = "MERGE INTO advancing_alive_workers aaw\n" +
                "USING (SELECT ? pod_name, CURRENT_TIMESTAMP heartbeat_timestamp  FROM dual) s\n" +
                "ON (aaw.pod_name = s.pod_name)\n" +
                "WHEN MATCHED THEN UPDATE SET aaw.heartbeat_timestamp = s.heartbeat_timestamp\n" +
                "WHEN NOT MATCHED THEN \n" +
                "INSERT (pod_name, heartbeat_timestamp) VALUES (?, current_timestamp)";
        advancingJdbcTemplate.update(sql, currentPodName, currentPodName);
    }

    public int insertPartition(String instanceUuid, String tokenUuid) {
        String sqlUpsert = "MERGE INTO advancing_partition ap\n" +
                "USING (SELECT ? instance_uuid, ? token_uuid, ? status  FROM dual) s\n" +
                "ON (ap.instance_uuid = s.instance_uuid AND ap.token_uuid = s.token_uuid)\n" +
                "WHEN MATCHED THEN UPDATE SET ap.status = s.status\n" +
                "WHEN NOT MATCHED THEN \n" +
                "INSERT (instance_uuid, token_uuid, status) VALUES (s.instance_uuid, s.token_uuid, s.status)";

        advancingJdbcTemplate.update(sqlUpsert, instanceUuid, tokenUuid, AdvancingPartitionStatus.NEW.name());

        String sqlSelect = "SELECT id FROM advancing_partition WHERE instance_uuid = ? AND token_uuid = ?";
        return advancingJdbcTemplate.queryForObject(sqlSelect, Integer.class, instanceUuid, tokenUuid);
    }
}
