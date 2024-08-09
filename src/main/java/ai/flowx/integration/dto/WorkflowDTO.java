package ai.flowx.integration.dto;

import ai.flowx.commons.definitions.audit.dtos.BaseAuditableEntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDTO extends BaseAuditableEntityDTO implements Serializable {
    private String id;
    private String flowxUuid;
    private String name;
    private String description;
}

