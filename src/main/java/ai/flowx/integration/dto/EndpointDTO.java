package ai.flowx.integration.dto;

import ai.flowx.commons.definitions.audit.dtos.BaseAuditableEntityDTO;
import ai.flowx.integration.config.HttpMethodSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpMethod;

import java.util.List;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndpointDTO extends BaseAuditableEntityDTO {
    @Id
    private String id;
    private String flowxUuid;
    private String name;
    private String url;
    @JsonSerialize(using = HttpMethodSerializer.class)
    private HttpMethod httpMethod;
    private String payload;
    private List<EndpointParamDTO> headers;
    private List<EndpointParamDTO> queryParameters;
    private List<EndpointResponseDTO> responses;
}


