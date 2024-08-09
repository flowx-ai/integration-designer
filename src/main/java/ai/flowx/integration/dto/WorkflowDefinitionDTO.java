package ai.flowx.integration.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@Setter
public class WorkflowDefinitionDTO extends WorkflowDTO {
    private List<WorkflowNodeDTO> nodes;
    private List<EndpointWithSystemSummaryDTO> endpoints;
}

