package ai.flowx.integration.dto;

import ai.flowx.commons.definitions.audit.dtos.BaseAuditableEntityDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntegrationSystemSummaryDTO extends BaseAuditableEntityDTO {
    private String id;
    private String flowxUuid;
    @NotNull
    private String name;
    @NotNull
    private String code;
    private String baseUrl;
    private String description;
}
