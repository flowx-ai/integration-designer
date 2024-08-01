package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.*;
import ai.flowx.integration.dto.WorkflowDTO;
import ai.flowx.integration.dto.WorkflowDefinitionDTO;
import ai.flowx.integration.dto.WorkflowWithSystemsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface WorkflowMapper {
    WorkflowDTO toDto(Workflow entity);

    @Mapping(target = "systems", expression = "java(mapSystems(entity.getSystems(), entity.getIntegrationsSystems()))")
    WorkflowWithSystemsDTO toDto(WorkflowWithSystemsCodes entity);

    Workflow toEntity(WorkflowDTO dto);

    WorkflowDefinitionDTO toDefinitionDto(WorkflowDefinition entity);

    default List<IntegrationSystemCode> mapSystems(List<WorkflowIntegrationSystem> systems, List<IntegrationSystemCode> integrationsSystems) {
        if (CollectionUtils.isEmpty(systems)) {
            return List.of();
        }
        Set<String> systemsFlowxUuids = systems.stream()
                .filter(s -> s.getCounter() > 0)
                .map(WorkflowIntegrationSystem::getIntegrationSystemFlowxUuid)
                .collect(Collectors.toSet());
        return integrationsSystems.stream()
                .filter(s -> systemsFlowxUuids.contains(s.getFlowxUuid()))
                .collect(toList());
    }
}
