package ai.flowx.integration.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndpointWithSystemSummary extends Endpoint {
    private EndpointIntegrationSystemSummary system;
}
