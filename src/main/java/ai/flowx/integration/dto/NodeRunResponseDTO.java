package ai.flowx.integration.dto;

import ai.flowx.integration.domain.enums.StatusType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeRunResponseDTO {
    private Object output;
    private StatusType status;
    private String passedConditionId;
}
