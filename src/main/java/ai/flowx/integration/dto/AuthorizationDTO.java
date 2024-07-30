package ai.flowx.integration.dto;

import ai.flowx.integration.domain.enums.AuthorizationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationDTO {
    private AuthorizationType type;
    private Map<String, Object> configuration;
}
