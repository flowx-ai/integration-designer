package ai.flowx.integration.advancing.worker;

import ai.flowx.integration.advancing.config.AdvancingProperties;
import ai.flowx.integration.advancing.dto.AdvanceTokenNotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Consumer;

@Builder
@Getter
public class AdvancingWorkerConfiguration {
    private String currentPodName;
    private String dbType;
    private AdvancingProperties advancingProperties;
    private ObjectMapper objectMapper;
    private Consumer<AdvancingWorker> removeWorkerFunction;
    private Consumer<AdvanceTokenNotificationDTO> processAdvancingEventFunction;
}
