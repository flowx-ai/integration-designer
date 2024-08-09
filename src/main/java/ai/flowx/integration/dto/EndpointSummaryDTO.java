package ai.flowx.integration.dto;

import ai.flowx.commons.definitions.audit.dtos.BaseAuditableEntityDTO;
import ai.flowx.integration.config.HttpMethodSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpMethod;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointSummaryDTO extends BaseAuditableEntityDTO {
    private String id;
    private String flowxUuid;
    @NotNull
    private String name;
    private String description;
    private String url;
    @NotNull @JsonSerialize(using = HttpMethodSerializer.class)
    private HttpMethod httpMethod;
    private String payload;
}


