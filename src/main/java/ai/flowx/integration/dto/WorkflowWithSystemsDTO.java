package ai.flowx.integration.dto;

import ai.flowx.commons.definitions.audit.dtos.BaseAuditableEntityDTO;
import ai.flowx.integration.domain.IntegrationSystemCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowWithSystemsDTO extends WorkflowDTO {
    private List<IntegrationSystemCode> systems;
}

