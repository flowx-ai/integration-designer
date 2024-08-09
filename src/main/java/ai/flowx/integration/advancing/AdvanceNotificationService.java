package ai.flowx.integration.advancing;

import ai.flowx.integration.instances.domain.TokenNodeStatus;

import java.util.UUID;

public interface AdvanceNotificationService {

    void notifyAdvanceToken(UUID wiUuid, UUID tiUuid, UUID fromNodeId, TokenNodeStatus tokenNodeStatus);

    void finishAdvancingPartition(String wiUuid);

}
