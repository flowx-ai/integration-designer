package ai.flowx.integration.dto;

import ai.flowx.integration.domain.enums.ConditionType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionDTO {
    private String id;
    private String expression;
    private ConditionType type;
    private int order;
}
