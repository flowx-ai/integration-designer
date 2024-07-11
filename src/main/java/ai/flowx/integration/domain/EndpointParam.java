package ai.flowx.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointParam {
    private String id;
    private String key;
    private String defaultValue;
    private String description;
    private boolean required;
}
