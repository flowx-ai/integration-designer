package ai.flowx.integration.mapper;

import ai.flowx.integration.domain.Sequence;
import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.dto.SequenceDTO;
import ai.flowx.integration.dto.UpdateWorkflowNodeReqDTO;
import ai.flowx.integration.dto.WorkflowNodeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface WorkflowNodeMapper {
    WorkflowNodeDTO toDto(WorkflowNode entity);

    WorkflowNode toEntity(WorkflowNodeDTO dto);

    WorkflowNode toEntity(UpdateWorkflowNodeReqDTO dto);

    Sequence toSequenceEntity(SequenceDTO sequenceDTO);

    List<SequenceDTO> toSequenceDTOs(List<Sequence> sequences);
}
