package ai.flowx.integration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointParamDTO {
    private String id;
    @NotNull
    private String key;
    private String defaultValue;
    private String description;
    private boolean required;
}
