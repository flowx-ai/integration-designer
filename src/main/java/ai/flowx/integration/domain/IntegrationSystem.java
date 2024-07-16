package ai.flowx.integration.domain;

import ai.flowx.commons.definitions.audit.domain.BaseAuditableEntity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "integration_systems")
public class IntegrationSystem extends BaseAuditableEntity implements Serializable {
    @Id
    private String id;
    private String flowxUuid;
    private String name;
    private String code;
    private String baseUrl;
    private String description;
    private Authorization authorization;
    private List<Variable> variables;
}

