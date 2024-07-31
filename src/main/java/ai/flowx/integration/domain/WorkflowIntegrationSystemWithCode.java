package ai.flowx.integration.domain;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WorkflowIntegrationSystemWithCode {
    private String integrationSystemFlowxUuid;
    private int counter;
    private List<IntegrationSystemCode> integrationsSystems;
    private String code;
}
