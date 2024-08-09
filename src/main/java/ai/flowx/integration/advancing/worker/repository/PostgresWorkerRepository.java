package ai.flowx.integration.advancing.worker.repository;

import ai.flowx.integration.advancing.config.AdvancingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class PostgresWorkerRepository extends WorkerRepository {

    public PostgresWorkerRepository(AdvancingProperties advancingProperties, ObjectMapper objectMapper) {
        super(advancingProperties, objectMapper);
    }

    protected PreparedStatement getPreparedStatementPickEvents(Integer pickedPartitionsSize) {
        String sqlPickEvents =
                "select id from (\n" +
                        "select partition_id, id, rank() over (partition by partition_id order by id) as rank \n" +
                        "from advancing_partition_event \n" +
                        "where partition_id IN (_PARTITION_PARAMS_) and processing_status in ('NEW','STARTED') \n" +
                        ") as f \n" +
                        "where rank = 1 ";

        PreparedStatement preparedStatement = preparedStatementsPickEventsMap.get(pickedPartitionsSize);
        if (preparedStatement == null) {
            String sql = sqlPickEvents.replace("_PARTITION_PARAMS_", StringUtils.repeat("?", ",", pickedPartitionsSize));
            preparedStatement = prepareSql(sql);
            preparedStatementsPickEventsMap.put(pickedPartitionsSize, preparedStatement);
        }
        return preparedStatement;
    }

    public List<Integer> pickPartition(String podName, int threadIndex) throws SQLException {
        List<Integer> pickedPartitionIds = new LinkedList<>();

        String sqlPickPartition =
                "update advancing_status set picking_pod = ?, picking_thread_index = ?, picking_time = ? \n" +
                        "where id IN " +
                        "(select id \n" +
                        "from advancing_status where picking_pod is null \n" +
                        "order by id asc limit ? \n" +
                        "for update skip locked)" +
                        "returning partition_id";

        PreparedStatement preparedStatement = getPreparedStatement(sqlPickPartition);
        preparedStatement.setString(1, podName);
        preparedStatement.setInt(2, threadIndex);
        preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        preparedStatement.setInt(4, pickingBatchSize);

        try (ResultSet rs = preparedStatement.executeQuery();) {
            while (rs.next()) {
                int pickedPartitionId = rs.getInt(1);
                pickedPartitionIds.add(pickedPartitionId);
            }
        } catch (SQLException e) {
            log.error("Could not pick partition because exception " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return pickedPartitionIds;
    }
}
