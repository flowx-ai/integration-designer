package ai.flowx.integration.domain;

import com.fasterxml.jackson.annotation.JsonMerge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointParam {
    @JsonMerge
    private String id;
    private String key;
    private String defaultValue;
    private String description;
    private boolean required;
}
