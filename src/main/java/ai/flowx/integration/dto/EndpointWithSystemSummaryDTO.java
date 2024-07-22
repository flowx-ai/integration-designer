package ai.flowx.integration.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class EndpointWithSystemSummaryDTO extends EndpointDTO {
    private EndpointIntegrationSystemSummaryDTO system;
}
