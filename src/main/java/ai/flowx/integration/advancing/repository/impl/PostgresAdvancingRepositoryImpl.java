package ai.flowx.integration.advancing.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

import static ai.flowx.integration.advancing.dto.enums.AdvancingPartitionStatus.NEW;

@ConditionalOnExpression("'${spring.jpa.database}' == 'postgresql'")
@Slf4j
@Repository
public class PostgresAdvancingRepositoryImpl extends AdvancingRepositoryImpl{

    public PostgresAdvancingRepositoryImpl(JdbcTemplate advancingJdbcTemplate, ObjectMapper objectMapper){
        super(advancingJdbcTemplate, objectMapper);
    }

    public void updateHeartbeat(String currentPodName) {
        String sql = "with try_update as (update advancing_alive_workers set heartbeat_timestamp = now() where pod_name = ? returning pod_name)\n" +
                "insert into advancing_alive_workers (pod_name, heartbeat_timestamp) select ?, now() where not exists (select * from try_update)";
        advancingJdbcTemplate.update(sql, currentPodName, currentPodName);
    }

    public int insertPartition(String instanceUuid, String tokenUuid) {
        String sql = "insert into advancing_partition (instance_uuid, token_uuid, status) values (?,?,?) " +
                "on conflict (instance_uuid, token_uuid) do update set status = EXCLUDED.status returning id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        advancingJdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                    ps.setString(1, instanceUuid);
                    ps.setString(2, tokenUuid);
                    ps.setString(3, NEW.name());
                    return ps;
                }, keyHolder);
        return keyHolder.getKey().intValue();
    }
}
