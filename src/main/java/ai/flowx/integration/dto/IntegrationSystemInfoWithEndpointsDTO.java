package ai.flowx.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationSystemInfoWithEndpointsDTO  {
    private String id;
    private String flowxUuid;
    private String name;
    private String code;
    private String description;
    private List<SystemEndpointSummaryDTO> endpoints;
}
