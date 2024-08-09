package ai.flowx.integration.dto;

import ai.flowx.integration.dto.enums.ParamType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericEndpointParamDTO extends EndpointParamDTO{
    @NotNull
    private ParamType paramType;
}
