package ai.flowx.integration.domain;

import ai.flowx.integration.domain.enums.ConditionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Condition {
    private String id;
    private String expression;
    private ConditionType type;
    private int order;
}
