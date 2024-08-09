package ai.flowx.integration.advancing.worker.repository;

import ai.flowx.integration.advancing.dto.AdvanceTokenNotificationDTO;
import ai.flowx.integration.advancing.dto.AdvancingEventDTO;
import ai.flowx.integration.advancing.dto.enums.AdvanceNotificationEventType;
import ai.flowx.integration.instances.domain.TokenNodeStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


@Slf4j
@RequiredArgsConstructor
public class AdvancingEventPayloadRowMapper implements RowMapper<AdvancingEventDTO> {
    private final ObjectMapper objectMapper;

    @Override
    public AdvancingEventDTO mapRow(ResultSet rs, int k) throws SQLException {
        AdvancingEventDTO event = new AdvancingEventDTO();
        AdvanceTokenNotificationDTO notification = new AdvanceTokenNotificationDTO();
        event.setAdvanceTokenNotificationDTO(notification);

        event.setId(rs.getInt("id"));
        event.setPartitionId(rs.getInt("partition_id"));
        event.setInstanceUuid(rs.getString("instance_uuid"));
        notification.setWInstanceUuid(rs.getString("instance_uuid"));
        notification.setTokenUuid(rs.getString("token_uuid"));
        notification.setEventType(AdvanceNotificationEventType.valueOf(rs.getString("type")));

        String fromNode = rs.getString("from_node_uuid");
        if (!rs.wasNull()) {
            notification.setFromNodeId(fromNode);
        }

        String tokenNodeStatus = rs.getString("token_node_status");
        if (StringUtils.isNotEmpty(tokenNodeStatus)) {
            notification.setTokenNodeStatus(TokenNodeStatus.valueOf(tokenNodeStatus));
        }

        return event;
    }
}
