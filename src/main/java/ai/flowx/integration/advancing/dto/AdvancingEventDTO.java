package ai.flowx.integration.advancing.dto;

import ai.flowx.integration.advancing.dto.enums.AdvancingEventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvancingEventDTO {
    private int id;
    private int partitionId;
    private String instanceUuid;
    private AdvanceTokenNotificationDTO advanceTokenNotificationDTO;
    private LocalDateTime creationTimestamp;
    private AdvancingEventStatus processingStatus;
    private LocalDateTime processingStartTimestamp;
    private LocalDateTime processingEndTimestamp;
}
