package ai.flowx.integration.advancing.dto;

import ai.flowx.integration.advancing.dto.enums.AdvanceNotificationEventType;
import ai.flowx.integration.instances.domain.TokenNodeStatus;
import lombok.*;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdvanceTokenNotificationDTO {
    private AdvanceNotificationEventType eventType;
    private String wInstanceUuid;
    private String tokenUuid;
    private String fromNodeId;
    private TokenNodeStatus tokenNodeStatus;
}
