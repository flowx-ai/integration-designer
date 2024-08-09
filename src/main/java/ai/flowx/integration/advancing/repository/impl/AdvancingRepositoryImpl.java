package ai.flowx.integration.advancing.repository.impl;

import ai.flowx.integration.advancing.repository.AdvancingRepository;
import ai.flowx.integration.advancing.dto.AdvanceTokenNotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static ai.flowx.integration.advancing.dto.enums.AdvancingPartitionStatus.FINISHED;
import static ai.flowx.integration.advancing.dto.enums.AdvancingPartitionStatus.NEW;


@Slf4j
@Repository
public abstract class AdvancingRepositoryImpl implements AdvancingRepository {

    protected JdbcTemplate advancingJdbcTemplate;

    protected final ObjectMapper objectMapper;

    public AdvancingRepositoryImpl(@Qualifier("advancingJdbcTemplate") JdbcTemplate advancingJdbcTemplate, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.advancingJdbcTemplate = advancingJdbcTemplate;
    }

    public Integer findPartitionId(String instanceUuid, String tokenUuid) {
        try {
            return advancingJdbcTemplate.queryForObject(SQL_FIND_PARTITION_BY_UUIDS, Integer.class, instanceUuid, tokenUuid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void finishAdvancingPartition(String instanceUuid) {
        advancingJdbcTemplate.update(SQL_UPDATE_PARTITION_STATUS, FINISHED.name(), instanceUuid);
    }

    public void insertAdvancingEvent(int partitionId, AdvanceTokenNotificationDTO advanceNotification) {
        advancingJdbcTemplate.update(SQL_INSERT_ADVANCING_EVENT,
                partitionId,
                advanceNotification.getEventType() != null ? advanceNotification.getEventType().name() : null,
                advanceNotification.getFromNodeId(),
                advanceNotification.getTokenNodeStatus() != null ? advanceNotification.getTokenNodeStatus().name() : null,
                new Timestamp(System.currentTimeMillis()),
                NEW.name()
        );
    }

}
