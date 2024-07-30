package ai.flowx.integration.dto;

import ai.flowx.integration.domain.enums.StatusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SequenceDTO {
    private String id;
    private String targetNodeFlowxUuid;
    private StatusType statusOutput;
    private String conditionId;
}
