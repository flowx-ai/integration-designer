package ai.flowx.integration.service;

import ai.flowx.commons.errors.BadRequestAlertException;
import ai.flowx.integration.domain.EndpointMetadata;
import ai.flowx.integration.domain.Sequence;
import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.StatusType;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.dto.*;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import ai.flowx.integration.mapper.WorkflowNodeMapper;
import ai.flowx.integration.repository.WorkflowNodeRepository;
import ai.flowx.integration.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ai.flowx.integration.domain.enums.WorkflowNodeType.SCRIPT;
import static ai.flowx.integration.domain.enums.WorkflowNodeType.START;
import static ai.flowx.integration.exceptions.ExceptionMessages.*;

@Transactional
@Component
public class WorkflowNodeService {
    public static final Set<WorkflowNodeType> TYPES_WITH_ONE_SEQUENCE_ALLOWED = Set.of(START, SCRIPT);
    public static final Set<WorkflowNodeType> START_AND_END_NODE_TYPES = Set.of(START, WorkflowNodeType.END);

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
                START, this::generalUpdateNode,
                WorkflowNodeType.END, this::generalUpdateNode,
                WorkflowNodeType.REST, this::updateRestNode,
                WorkflowNodeType.FORK, this::generalUpdateNode,
                SCRIPT, this::generalUpdateNode
        );
    }

    public void createStartNodeForWorkflow(String workflowId) {
        WorkflowNode node = new WorkflowNode();
        node.setName("start");
        node.setFlowxUuid(UUID.randomUUID().toString());
        node.setType(START);
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
        if (START.equals(dto.getType())) {
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


    public SequenceDTO createSequenceOnWorkflowNode(String workflowNodeId, SequenceDTO sequenceDTO) {
        sequenceDTO.setId(UUID.randomUUID().toString());

        List<WorkflowNode> workflowNodes = workflowNodeRepository.findByIdOrFlowxUuid(workflowNodeId, sequenceDTO.getTargetNodeFlowxUuid());
        if(workflowNodes.size() != 2) {
            throw new BadRequestAlertException(WORKFLOW_NODE_NOT_FOUND, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_NOT_FOUND);
        }

        WorkflowNode sourceWorkflowNode = workflowNodes.stream().filter(s -> s.getId().equals(workflowNodeId)).findFirst().get();
        WorkflowNode targetWorkflowNode = workflowNodes.stream().filter(s -> s.getFlowxUuid().equals(sequenceDTO.getTargetNodeFlowxUuid())).findFirst().get();

        validateSequenceForSourceWorkflowNode(sequenceDTO, sourceWorkflowNode);
        validateSourceAndTargetSequenceCompatibility(sourceWorkflowNode, targetWorkflowNode);

        workflowNodeRepository.addSequence(workflowNodeId, workflowNodeMapper.toSequenceEntity(sequenceDTO));

        return sequenceDTO;
    }

    private static void validateSequenceForSourceWorkflowNode(SequenceDTO sequenceDTO, WorkflowNode sourceWorkflowNode) {
        boolean contentsOfDTOsNotConsistentWithType = sequenceDTO.getStatusOutput() != null && !sourceWorkflowNode.getType().equals(WorkflowNodeType.REST) ||
                WorkflowNodeType.REST.equals(sourceWorkflowNode.getType()) && sequenceDTO.getStatusOutput() == null ||
                sequenceDTO.getConditionId() != null && !WorkflowNodeType.FORK.equals(sourceWorkflowNode.getType()) ||
                sequenceDTO.getConditionId() == null && WorkflowNodeType.FORK.equals(sourceWorkflowNode.getType());

        if(contentsOfDTOsNotConsistentWithType
        ) {
            throw new BadRequestAlertException(WORKFLOW_NODE_SEQUENCE_NOT_VALID, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_SEQUENCE_NOT_VALID);
        }

        if(TYPES_WITH_ONE_SEQUENCE_ALLOWED.contains(sourceWorkflowNode.getType()) && !CollectionUtils.isEmpty(sourceWorkflowNode.getOutgoingSequences())) {
            throw new BadRequestAlertException(WORKFLOW_NODE_SEQUENCE_NOT_VALID_ONE_SEQUENCE_ALLOWED, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_SEQUENCE_NOT_VALID);
        } else if (WorkflowNodeType.END.equals(sourceWorkflowNode.getType())) {
            throw new BadRequestAlertException(WORKFLOW_NODE_SEQUENCE_NOT_VALID_END_NODE_NOT_ALLOWED, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_SEQUENCE_NOT_VALID);
        } else if (WorkflowNodeType.REST.equals(sourceWorkflowNode.getType())) {
            Map<StatusType, Long> collect = Optional.ofNullable(sourceWorkflowNode.getOutgoingSequences()).orElseGet(Collections::emptyList)
                    .stream()
                    .map(Sequence::getStatusOutput)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            if (collect.getOrDefault(sequenceDTO.getStatusOutput(), 0L) > 0) {
                throw new BadRequestAlertException(WORKFLOW_NODE_SEQUENCE_NOT_VALID_REST_NODE_HAS_STATUS_OUTPUT, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_SEQUENCE_NOT_VALID);
            }
        } else if(WorkflowNodeType.FORK.equals(sourceWorkflowNode.getType())) {
            Optional.ofNullable(sourceWorkflowNode.getConditions()).orElse(Collections.emptyList())
                    .stream()
                    .filter(s -> s.getId().equals(sequenceDTO.getConditionId())).findFirst()
                    .orElseThrow(() -> new BadRequestAlertException(WORKFLOW_NODE_SEQUENCE_NOT_VALID_FORK_CONDITION_ID_NOT_FOUND, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_SEQUENCE_NOT_VALID));
        }
    }

    private void validateSourceAndTargetSequenceCompatibility(WorkflowNode sourceWorkflowNode, WorkflowNode targetWorkflowNode) {
        if(START_AND_END_NODE_TYPES.contains(sourceWorkflowNode.getType()) &&
                START_AND_END_NODE_TYPES.contains(targetWorkflowNode.getType())) {
            throw new BadRequestAlertException(WORKFLOW_NODE_SEQUENCE_NOT_VALID_SRC_TARGET_BOTH_START_END, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_SEQUENCE_NOT_VALID);
        }

        if(targetWorkflowNode.getType().equals(WorkflowNodeType.END) && sourceWorkflowNode.getType().equals(START)) {
            throw new BadRequestAlertException(WORKFLOW_NODE_SEQUENCE_NOT_VALID_START_TO_END, SequenceDTO.class.getName(), BadRequestErrorType.WORKFLOW_NODE_SEQUENCE_NOT_VALID);
        }
    }
}
