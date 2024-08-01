package ai.flowx.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationSystemEndpoint {
    private String id;
    private String flowxUuid;
    private String name;
    private String code;
    private String description;
    private EndpointSummary endpoints;
}

