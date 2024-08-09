package ai.flowx.integration.domain;

import ai.flowx.integration.domain.enums.StatusType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sequence {
    private String id;
    private String targetNodeFlowxUuid;
    private StatusType statusOutput;
    private String conditionId;
}
