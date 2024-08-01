package ai.flowx.integration.dto;

import ai.flowx.integration.domain.enums.StatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SequenceDTO {
    @NotNull
    private String id;
    @NotNull
    private String targetNodeFlowxUuid;
    private StatusType statusOutput;
    private String conditionId;
}
