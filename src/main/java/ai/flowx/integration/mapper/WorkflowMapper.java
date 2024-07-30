package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.Workflow;
import ai.flowx.integration.domain.WorkflowDefinition;
import ai.flowx.integration.dto.WorkflowDTO;
import ai.flowx.integration.dto.WorkflowDefinitionDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WorkflowMapper {
    WorkflowDTO toDto(Workflow entity);

    Workflow toEntity(WorkflowDTO dto);

    WorkflowDefinitionDTO toDefinitionDto(WorkflowDefinition entity);

}
