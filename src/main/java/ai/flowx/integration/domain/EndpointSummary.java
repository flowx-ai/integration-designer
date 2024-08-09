package ai.flowx.integration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointSummary {
    private String id;
    private String flowxUuid;
    private String name;
    private HttpMethod httpMethod;
    private String description;
}
