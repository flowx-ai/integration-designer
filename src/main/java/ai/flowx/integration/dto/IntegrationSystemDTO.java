package ai.flowx.integration.dto;

import ai.flowx.commons.definitions.audit.dtos.BaseAuditableEntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationSystemDTO extends BaseAuditableEntityDTO {
    private String id;
    private String flowxUuid;
    private String name;
    private String code;
    private String baseUrl;
    private String description;
    private AuthorizationDTO authorization;
    private List<VariableDTO> variables;
}

