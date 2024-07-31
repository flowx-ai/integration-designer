package ai.flowx.integration.service;

import ai.flowx.commons.errors.BadRequestAlertException;
import ai.flowx.integration.domain.EndpointMetadata;
import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.dto.CreateWorkflowNodeReqDTO;
import ai.flowx.integration.dto.UpdateWorkflowNodeReqDTO;
import ai.flowx.integration.dto.WorkflowNodeDTO;
import ai.flowx.integration.dto.WorkflowNodePositionDTO;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import ai.flowx.integration.mapper.WorkflowNodeMapper;
import ai.flowx.integration.repository.WorkflowNodeRepository;
import ai.flowx.integration.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import static ai.flowx.integration.exceptions.ExceptionMessages.*;

@Transactional
@Component
public class WorkflowNodeService {
    private final WorkflowNodeRepository workflowNodeRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final UpdateWorkflowNodeValidator updateWorkflowNodeValidator;
    private final EndpointService endpointService;
    private Map<WorkflowNodeType, BiConsumer<WorkflowNode, UpdateWorkflowNodeReqDTO>> updateNodeFunctions;

    public WorkflowNodeService(WorkflowNodeRepository workflowNodeRepository, WorkflowRepository workflowRepository,
                               WorkflowNodeMapper workflowNodeMapper, UpdateWorkflowNodeValidator updateWorkflowNodeValidator,
                               EndpointService endpointService) {
        this.workflowNodeRepository = workflowNodeRepository;
        this.workflowRepository = workflowRepository;
        this.workflowNodeMapper = workflowNodeMapper;
        this.updateWorkflowNodeValidator = updateWorkflowNodeValidator;
        this.endpointService = endpointService;
        updateNodeFunctions = Map.of(
                WorkflowNodeType.START, this::generalUpdateNode,
                WorkflowNodeType.END, this::generalUpdateNode,
                WorkflowNodeType.REST, this::updateRestNode,
                WorkflowNodeType.FORK, this::generalUpdateNode,
                WorkflowNodeType.SCRIPT, this::generalUpdateNode
        );
    }

    public void createStartNodeForWorkflow(String workflowId) {
        WorkflowNode node = new WorkflowNode();
        node.setName("start");
        node.setFlowxUuid(UUID.randomUUID().toString());
        node.setType(WorkflowNodeType.START);
        node.setWorkflowId(workflowId);
        node.setLayoutOptions(Map.of("x", 0, "y", 0));
        workflowNodeRepository.save(node);
    }

    public WorkflowNodeDTO createNode(CreateWorkflowNodeReqDTO createWorkflowNodeReqDTO) {
        validateNodeTypeForCreate(createWorkflowNodeReqDTO);
        validateName(createWorkflowNodeReqDTO.getName());
        validateWorkflowId(createWorkflowNodeReqDTO.getWorkflowId());
        WorkflowNode node = new WorkflowNode();
        node.setName(createWorkflowNodeReqDTO.getName());
        node.setFlowxUuid(UUID.randomUUID().toString());
        node.setType(createWorkflowNodeReqDTO.getType());
        node.setWorkflowId(createWorkflowNodeReqDTO.getWorkflowId());
        node.setLayoutOptions(createWorkflowNodeReqDTO.getLayoutOptions());
        return workflowNodeMapper.toDto(workflowNodeRepository.save(node));
    }

    private void validateNodeTypeForCreate(CreateWorkflowNodeReqDTO dto) {
        if (WorkflowNodeType.START.equals(dto.getType())) {
            throw new BadRequestAlertException(INVALID_NODE_TYPE, WorkflowNode.class.getName(),
                    BadRequestErrorType.INVALID_NODE_TYPE);
        }
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestAlertException(WORKFLOW_NODE_NAME_REQUIRED, WorkflowNode.class.getName(),
                    BadRequestErrorType.WORKFLOW_NODE_NAME_REQUIRED);
        }
    }

    private void validateWorkflowId(String workflowId) {
        if (!workflowRepository.existsById(workflowId)) {
            throw new BadRequestAlertException(WORKFLOW_NOT_FOUND, WorkflowNode.class.getName(),
                    BadRequestErrorType.WORKFLOW_NOT_FOUND);
        }
    }

    public Set<WorkflowNodePositionDTO> updateLayoutOptions(Set<WorkflowNodePositionDTO> workflowNodePositions) {
        workflowNodeRepository.bulkUpdateLayoutOptions(workflowNodePositions);
        return workflowNodePositions;
    }

    public WorkflowNodeDTO updateWorkflowNode(UpdateWorkflowNodeReqDTO updateWorkflowNodeDTO) {
        WorkflowNode existingNode = findOneMandatory(updateWorkflowNodeDTO.getId());

        updateWorkflowNodeValidator.validate(existingNode.getType(), updateWorkflowNodeDTO);

        updateNodeFunctions.get(existingNode.getType()).accept(existingNode, updateWorkflowNodeDTO);

        return workflowNodeMapper.toDto(findOneMandatory(updateWorkflowNodeDTO.getId()));
    }

    private void generalUpdateNode(WorkflowNode existingNode, UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
        WorkflowNode nodeToUpdate = workflowNodeMapper.toEntity(updateWorkflowNodeReqDTO);
        workflowNodeRepository.updateGeneralNode(nodeToUpdate);
    }

    private void updateRestNode(WorkflowNode existingNode, UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
        WorkflowNode nodeToUpdate = workflowNodeMapper.toEntity(updateWorkflowNodeReqDTO);
        if (Objects.equals(existingNode.getEndpointFlowxUuid(), nodeToUpdate.getEndpointFlowxUuid())) {
            nodeToUpdate.setIntegrationSystemFlowxUuid(existingNode.getIntegrationSystemFlowxUuid());
        } else {
            EndpointMetadata endpointMetadata = endpointService.getEndpointMetadata(nodeToUpdate.getEndpointFlowxUuid());
            nodeToUpdate.setIntegrationSystemFlowxUuid(endpointMetadata.getIntegrationSystemFlowxUuid());
            workflowRepository.updateSystemCounters(existingNode.getWorkflowId(), existingNode.getIntegrationSystemFlowxUuid(),
                    nodeToUpdate.getIntegrationSystemFlowxUuid());
        }
        workflowNodeRepository.updateGeneralNode(nodeToUpdate);
    }

    private WorkflowNode findOneMandatory(String id) {
        return workflowNodeRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(WORKFLOW_NODE_NOT_FOUND, WorkflowNode.class.getName(),
                        BadRequestErrorType.WORKFLOW_NODE_NOT_FOUND));
    }

}
