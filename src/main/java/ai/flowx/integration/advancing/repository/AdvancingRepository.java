package ai.flowx.integration.advancing.repository;

import ai.flowx.integration.advancing.dto.AdvanceTokenNotificationDTO;


public interface AdvancingRepository {
    String SQL_FIND_PARTITION_BY_UUIDS = "select id from advancing_partition where instance_uuid = ? and token_uuid = ?";
    String SQL_INSERT_ADVANCING_EVENT =
            "insert into advancing_partition_event " +
                    "(partition_id, type, from_node_uuid, token_node_status, creation_timestamp, processing_status) " +
                    "values (?,?,?,?,?,?)";
    String SQL_UPDATE_PARTITION_STATUS = "update advancing_partition set status = ? where instance_uuid = ?";

    void finishAdvancingPartition(String instanceUuid);

    void updateHeartbeat(String currentPodName);

    Integer findPartitionId(String instanceUuid, String tokenUuid);

    int insertPartition(String instanceUuid, String tokenUuid);

    void insertAdvancingEvent(int partitionId, AdvanceTokenNotificationDTO advanceNotification);
}
