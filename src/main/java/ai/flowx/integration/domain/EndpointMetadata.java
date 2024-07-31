package ai.flowx.integration.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndpointMetadata {
    private String id;
    private String flowxUuid;
    private String integrationSystemFlowxUuid;
}
