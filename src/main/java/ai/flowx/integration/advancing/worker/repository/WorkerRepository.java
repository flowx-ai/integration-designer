package ai.flowx.integration.advancing.worker.repository;

import ai.flowx.integration.advancing.config.AdvancingProperties;
import ai.flowx.integration.advancing.dto.AdvancingEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static ai.flowx.integration.advancing.dto.enums.AdvancingEventStatus.STARTED;
import static ai.flowx.integration.advancing.dto.enums.AdvancingEventStatus.FAILED;
import static ai.flowx.integration.advancing.dto.enums.AdvancingPartitionStatus.FINISHED;


@Slf4j
public abstract class WorkerRepository {

    private static String SQL_UPDATE_PROCESSING_START = "update advancing_partition_event  set processing_status = ?, processing_start_timestamp = ? where id = ?";
    private static String SQL_UPDATE_PROCESSING_FINISH = "update advancing_partition_event  set processing_status = ?, processing_end_timestamp = ? where id = ?";
    private static String SQL_UPDATE_PROCESSING_FAIL = "update advancing_partition_event  set processing_status = ?, processing_end_timestamp = ?, processing_error_message = ? where id = ?";
    private static String SQL_LOAD_EVENTS =
            "select b.instance_uuid, b.token_uuid, a.* \n" +
                    "from advancing_partition_event a\n" +
                    "join advancing_partition b on a.partition_id = b.id\n" +
                    "where a.id IN (_EVENT_IDS_) ";
    private static String SQL_CHECK_ADVANCING_WORK_QUEUED = "select count(*) from advancing_status where picking_pod is null";
    private static String SQL_UNPICK_ALL_PARTITIONS = "update advancing_status set picking_pod = null, picking_thread_index = null, picking_time = null where picking_pod = ? and picking_thread_index = ?";
    private static String SQL_RESET_PARTITION_EVENTS_COUNTER = "update advancing_partition set count_queued_events = 0 where id in (_PARTITION_IDS_) and (select count(*) from advancing_partition_event where partition_id in (_PARTITION_IDS_) and processing_status in ('NEW','STARTED')) = 0";

    protected AdvancingProperties advancingProperties;
    protected Connection connection;
    protected Map<String, PreparedStatement> preparedStatementsMap = new HashMap<>();
    protected Map<Integer, PreparedStatement> preparedStatementsPickEventsMap = new HashMap<>();
    protected Map<Integer, PreparedStatement> preparedStatementsLoadEventsMap = new HashMap<>();
    protected Map<Integer, PreparedStatement> preparedStatementsResetPartitionCounterMap = new HashMap<>();
    protected RowMapper<AdvancingEventDTO> advancingEventRowMapper;
    protected int pickingBatchSize;

    protected abstract PreparedStatement getPreparedStatementPickEvents(Integer pickedPartitionsSize);

    public abstract List<Integer> pickPartition(String podName, int threadIndex) throws SQLException;

    public WorkerRepository(AdvancingProperties advancingProperties, ObjectMapper objectMapper) {
        this.advancingProperties = advancingProperties;
        advancingEventRowMapper = new AdvancingEventPayloadRowMapper(objectMapper);
        this.pickingBatchSize = advancingProperties.getPickingBatchSize();
        connectDB();
    }

    public List<Integer> pickEventToProcess(List<Integer> pickedPartitionIds) throws SQLException {
        List<Integer> pickedEventIds = new LinkedList<>();

        PreparedStatement preparedStatement = getPreparedStatementPickEvents(pickedPartitionIds.size());
        int paramIndex = 0;
        for (Integer pickedPartitionId : pickedPartitionIds) {
            preparedStatement.setInt(++paramIndex, pickedPartitionId);
        }

        try (ResultSet rs = preparedStatement.executeQuery();) {
            while (rs.next()) {
                int pickedEventId = rs.getInt(1);
                pickedEventIds.add(pickedEventId);
            }
        } catch (SQLException e) {
            log.error("Could not pick event to process because exception " + e.getMessage(), e);
            throw e;
        }

        return pickedEventIds;
    }

    public void updateEventProcessingStarted(int eventId) throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement(SQL_UPDATE_PROCESSING_START);
        preparedStatement.setString(1, STARTED.name());
        preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        preparedStatement.setInt(3, eventId);
        preparedStatement.executeUpdate();
    }

    public void updateEventProcessingFinished(int eventId) throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement(SQL_UPDATE_PROCESSING_FINISH);
        preparedStatement.setString(1, FINISHED.name());
        preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        preparedStatement.setInt(3, eventId);
        preparedStatement.executeUpdate();
    }

    public void updateEventProcessingFailed(int eventId, String processingErrorMessage) throws SQLException {
        PreparedStatement preparedStatement = getPreparedStatement(SQL_UPDATE_PROCESSING_FAIL);
        preparedStatement.setString(1, FAILED.name());
        preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        preparedStatement.setString(3, processingErrorMessage);
        preparedStatement.setInt(4, eventId);
        preparedStatement.executeUpdate();
    }

    public List<AdvancingEventDTO> loadAdvancingEvents(List<Integer> eventIds) throws SQLException {
        List<AdvancingEventDTO> events = new LinkedList<>();

        PreparedStatement preparedStatement = getPreparedStatementLoadEvents(eventIds.size());
        int paramIndex = 0;
        for (Integer eventId : eventIds) {
            preparedStatement.setInt(++paramIndex, eventId);
        }

        try (ResultSet rs = preparedStatement.executeQuery();) {
            while (rs.next()) {
                AdvancingEventDTO eventDTO = advancingEventRowMapper.mapRow(rs, 0);
                events.add(eventDTO);
            }
        } catch (SQLException e) {
            log.error("Could not load advancing event because exception " + e.getMessage(), e);
            throw e;
        }

        return events;
    }

    public boolean isAdvancingWorkQueued() throws SQLException {
        boolean result = false;
        PreparedStatement preparedStatement = getPreparedStatement(SQL_CHECK_ADVANCING_WORK_QUEUED);
        try (ResultSet rs = preparedStatement.executeQuery();) {
            if (rs.next()) {
                int count = rs.getInt(1);
                result = count > 0;
            }
        } catch (SQLException e) {
            log.error("Could not load advancing event because exception " + e.getMessage(), e);
            throw e;
        }
        return result;
    }

    public void unpickAllPartitions(String podName, int threadIndex) {
        try {
            PreparedStatement preparedStatement = getPreparedStatement(SQL_UNPICK_ALL_PARTITIONS);
            preparedStatement.setString(1, podName);
            preparedStatement.setInt(2, threadIndex);
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                log.info("Unpicked " + affectedRows + " partitions picked by pod " + podName + " and thread index " + threadIndex + " !");
            }

        } catch (SQLException e) {
            log.error("Could not unpick all partitions because " + e.getMessage(), e);
        }
    }

    public void resetPartitionsCounter(List<Integer> partitionIds) {
        try {
            PreparedStatement preparedStatement = getPreparedStatementResetPartitionCounter(partitionIds.size());
            int paramIndex = 0;
            for (Integer partitionId : partitionIds) {
                preparedStatement.setInt(++paramIndex, partitionId);
            }
            for (Integer partitionId : partitionIds) {
                preparedStatement.setInt(++paramIndex, partitionId);
            }
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            log.error("Could not reset partition event counter for partitions " + partitionIds + " because " + e.getMessage(), e);
        }
    }

    public void cleanup() {
        closePreparedStatementsMap(preparedStatementsMap);
        closePreparedStatementsMap(preparedStatementsPickEventsMap);
        closePreparedStatementsMap(preparedStatementsLoadEventsMap);
        closeConnection();
        log.debug("Worker repository clean up finished ! ");
    }

    protected PreparedStatement getPreparedStatement(String sql) {
        PreparedStatement preparedStatement = preparedStatementsMap.get(sql);
        if (preparedStatement == null) {
            preparedStatement = prepareSql(sql);
            preparedStatementsMap.put(sql, preparedStatement);
        }
        return preparedStatement;
    }

    private PreparedStatement getPreparedStatementLoadEvents(Integer loadEventsSize) {
        PreparedStatement preparedStatement = preparedStatementsLoadEventsMap.get(loadEventsSize);
        if (preparedStatement == null) {
            String sql = SQL_LOAD_EVENTS.replace("_EVENT_IDS_", StringUtils.repeat("?", ",", loadEventsSize));
            preparedStatement = prepareSql(sql);
            preparedStatementsLoadEventsMap.put(loadEventsSize, preparedStatement);
        }
        return preparedStatement;
    }

    private PreparedStatement getPreparedStatementResetPartitionCounter(Integer partitionIdsSize) {
        PreparedStatement preparedStatement = preparedStatementsResetPartitionCounterMap.get(partitionIdsSize);
        if (preparedStatement == null) {
            String sql = SQL_RESET_PARTITION_EVENTS_COUNTER.replaceAll("_PARTITION_IDS_", StringUtils.repeat("?", ",", partitionIdsSize));
            preparedStatement = prepareSql(sql);
            preparedStatementsResetPartitionCounterMap.put(partitionIdsSize, preparedStatement);
        }
        return preparedStatement;
    }

    private void closePreparedStatementsMap(Map<?, PreparedStatement> map) {
        map.forEach((k, v) -> {
            try {
                v.close();
            } catch (SQLException e) {
                log.error("Could not close prepared statement because " + e.getMessage(), e);
            }
        });
        map.clear();
    }

    protected PreparedStatement prepareSql(String sql) {
        try {
            if (connection == null) {
                connectDB();
            }
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not prepare statement because " + e.getMessage() + " for sql: " + sql);
        }
    }

    private void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Could not close db connection because " + e.getMessage(), e);
        }
    }

    private void connectDB() {
        boolean connected = false;

        while (!connected) {
            try {
                AdvancingProperties.DatasourceProperties config = advancingProperties.getDatasource();
                Class.forName(config.getDriverClassName());
                this.connection = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());
                connected = true;
            } catch (Exception e) {
                log.error("Could not obtain connection because " + e.getMessage(), e);
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
            }
        }
    }

}
