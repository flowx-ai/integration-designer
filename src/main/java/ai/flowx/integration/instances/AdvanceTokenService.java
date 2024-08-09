package ai.flowx.integration.instances;

import ai.flowx.integration.advancing.dto.AdvanceTokenNotificationDTO;

public interface AdvanceTokenService {
    void advance(AdvanceTokenNotificationDTO advancingEvent);
}
