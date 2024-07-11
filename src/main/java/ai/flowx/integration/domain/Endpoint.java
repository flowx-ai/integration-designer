package ai.flowx.integration.domain;

import ai.flowx.commons.definitions.audit.domain.BaseAuditableEntity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "endpoints")
public class Endpoint extends BaseAuditableEntity implements Serializable {
    @Id
    private String id;
    private String flowxUuid;
    private String name;
    private String url;
    private HttpMethod httpMethod;
    private String payload;
    private List<EndpointParam> headers;
    private List<EndpointParam> queryParameters;
    private List<EndpointResponse> responses;
}


