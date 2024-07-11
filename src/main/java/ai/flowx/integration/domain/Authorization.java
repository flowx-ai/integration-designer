package ai.flowx.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Authorization {
    private AuthorizationType type;
    private Map<String, Object> configuration;
}
