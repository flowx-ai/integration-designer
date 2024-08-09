package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.Sequence;
import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.dto.SequenceDTO;
import ai.flowx.integration.dto.UpdateWorkflowNodeReqDTO;
import ai.flowx.integration.dto.WorkflowNodeDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface WorkflowNodeMapper {
    WorkflowNodeDTO toDto(WorkflowNode entity);

    WorkflowNode toEntity(WorkflowNodeDTO dto);

    WorkflowNode toEntity(UpdateWorkflowNodeReqDTO dto);

    Sequence toSequenceEntity(SequenceDTO sequenceDTO);

    List<SequenceDTO> toSequenceDTOs(List<Sequence> sequences);

    @AfterMapping
    default void setConditionId(@MappingTarget WorkflowNode entity) {
        if(!CollectionUtils.isEmpty(entity.getConditions())){
            entity.getConditions().forEach(condition -> {
                if(StringUtils.isEmpty(condition.getId())){
                    condition.setId(UUID.randomUUID().toString());
                }
            });
        }
    }
}
