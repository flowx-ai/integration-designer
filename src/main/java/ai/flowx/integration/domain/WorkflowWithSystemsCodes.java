package ai.flowx.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowWithSystemsCodes extends Workflow {
    private List<IntegrationSystemCode> integrationsSystems;
}

