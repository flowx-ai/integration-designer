package ai.flowx.integration.advancing;

import ai.flowx.integration.advancing.dto.AdvanceTokenNotificationDTO;
import ai.flowx.integration.advancing.dto.enums.AdvanceNotificationEventType;
import ai.flowx.integration.advancing.repository.AdvancingRepository;
import ai.flowx.integration.instances.domain.TokenNodeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AdvanceNotificationServiceImpl implements AdvanceNotificationService {
    private final AdvancingRepository advancingRepo;

    @Override
    public void notifyAdvanceToken(UUID wiUuid, UUID tiUuid, UUID fromNodeId, TokenNodeStatus tokenNodeStatus) {
        AdvanceTokenNotificationDTO advanceTokenNotif = AdvanceTokenNotificationDTO.builder()
                .eventType(AdvanceNotificationEventType.ADVANCE)
                .wInstanceUuid(wiUuid.toString())
                .tokenUuid(tiUuid.toString())
                .fromNodeId(fromNodeId.toString())
                .tokenNodeStatus(tokenNodeStatus)
                .build();

        advancingRepo.insertAdvancingEvent(findPartitionIdForNotif(advanceTokenNotif), advanceTokenNotif);
    }

    @Override
    public void finishAdvancingPartition(String wiUuid) {
        advancingRepo.finishAdvancingPartition(wiUuid);
    }

    private Integer findPartitionIdForNotif(AdvanceTokenNotificationDTO advanceNotification) {
        Integer partitionId = advancingRepo.findPartitionId(advanceNotification.getWInstanceUuid(), advanceNotification.getTokenUuid());
        if (partitionId == null) {
            partitionId = advancingRepo.insertPartition(advanceNotification.getWInstanceUuid(), advanceNotification.getTokenUuid());
        }

        return partitionId;
    }
}
