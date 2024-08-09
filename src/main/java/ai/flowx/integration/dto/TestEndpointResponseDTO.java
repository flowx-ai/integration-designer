package ai.flowx.integration.dto;

import ai.flowx.integration.config.HttpStatusDeserializer;
import ai.flowx.integration.config.HttpStatusSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestEndpointResponseDTO {
    @JsonDeserialize(using = HttpStatusDeserializer.class)
    @JsonSerialize(using = HttpStatusSerializer.class)
    private HttpStatus code;
    private Object body;
    private String curlCommand;
    private long responseTime;
    private long responseSize;
}