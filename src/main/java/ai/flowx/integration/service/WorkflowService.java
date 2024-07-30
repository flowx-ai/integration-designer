package ai.flowx.integration.service;

import ai.flowx.commons.errors.BadRequestAlertException;
import ai.flowx.integration.domain.Workflow;
import ai.flowx.integration.dto.WorkflowDTO;
import ai.flowx.integration.dto.WorkflowDefinitionDTO;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import ai.flowx.integration.mapper.WorkflowMapper;
import ai.flowx.integration.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static ai.flowx.integration.exceptions.ExceptionMessages.*;

@RequiredArgsConstructor
@Component
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;
    private final WorkflowNodeService workflowNodeService;

    public List<WorkflowDTO> getAllWorkflows() {
        return workflowRepository.findAll().stream()
                .map(workflowMapper::toDto).collect(Collectors.toList());
    }

    public Optional<WorkflowDefinitionDTO> findOneById(String id) {
        return workflowRepository.getWorkflowWithNodes(id).map(workflowMapper::toDefinitionDto);
    }

    public WorkflowDTO createWorkflow(WorkflowDTO workflowDTO) {
        if (workflowDTO.getId() != null) {
            throw new BadRequestAlertException(INVALID_ID, Workflow.class.getName(),
                    BadRequestErrorType.INVALID_ID);
        }
        validateName(workflowDTO.getName());

        Workflow entity = workflowMapper.toEntity(workflowDTO);
        entity.setFlowxUuid(UUID.randomUUID().toString());
        Workflow entitySaved = workflowRepository.save(entity);
        workflowNodeService.createStartNodeForWorkflow(entitySaved.getId());

        return workflowMapper.toDto(entitySaved);
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestAlertException(WORKFLOW_NAME_REQUIRED, Workflow.class.getName(),
                    BadRequestErrorType.WORKFLOW_NAME_REQUIRED);
        }
        if (workflowRepository.existsByName(name)) {
            throw new BadRequestAlertException(WORKFLOW_NAME_EXISTS, Workflow.class.getName(),
                    BadRequestErrorType.WORKFLOW_NAME_EXISTS);
        }
    }


    public WorkflowDTO updateWorkflow(WorkflowDTO workflowDTO) {
        Workflow existingEntity = workflowRepository.findById(workflowDTO.getId())
                .orElseThrow(() -> new BadRequestAlertException(WORKFLOW_NOT_FOUND, Workflow.class.getName(),
                        BadRequestErrorType.WORKFLOW_NOT_FOUND));
        if(!Objects.equals(existingEntity.getName(), workflowDTO.getName())) {
            validateName(workflowDTO.getName());
        }
        existingEntity.setName(workflowDTO.getName());
        existingEntity.setDescription(workflowDTO.getDescription());
        return workflowMapper.toDto(workflowRepository.save(existingEntity));
    }

    public void deleteWorkflow(String workflowId) {
        workflowRepository.deleteById(workflowId);
    }
}
