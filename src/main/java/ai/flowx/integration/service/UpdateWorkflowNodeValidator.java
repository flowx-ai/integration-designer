package ai.flowx.integration.service;

import ai.flowx.commons.errors.BadRequestAlertException;
import ai.flowx.integration.domain.WorkflowNode;
import ai.flowx.integration.domain.enums.WorkflowNodeType;
import ai.flowx.integration.dto.UpdateWorkflowNodeReqDTO;
import ai.flowx.integration.exceptions.enums.BadRequestErrorType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class UpdateWorkflowNodeValidator {
    private Map<WorkflowNodeType, Consumer<UpdateWorkflowNodeReqDTO>> validators;

    public UpdateWorkflowNodeValidator() {
        validators = Map.of(
                WorkflowNodeType.START, this::validateStartNode,
                WorkflowNodeType.END, this::validateEndNode,
                WorkflowNodeType.REST, this::validateRestNode,
                WorkflowNodeType.FORK, this::validateForkNode,
                WorkflowNodeType.SCRIPT, this::validateScriptNode
        );
    }

    private void validateScriptNode(UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
    }

    private void validateForkNode(UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
        if (CollectionUtils.isEmpty(updateWorkflowNodeReqDTO.getConditions())) {
            throw new BadRequestAlertException("Conditions must be provided for fork node", WorkflowNode.class.getName(),
                    BadRequestErrorType.WORKFLOW_NOT_UPDATED);
        }
        updateWorkflowNodeReqDTO.getConditions().stream().filter(condition -> condition.getType() == null)
                .findAny().ifPresent(condition -> {
                    throw new BadRequestAlertException("Type must be provided for each condition", WorkflowNode.class.getName(),
                            BadRequestErrorType.WORKFLOW_NOT_UPDATED);
                });
    }

    private void validateRestNode(UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
        if (!StringUtils.hasText(updateWorkflowNodeReqDTO.getEndpointFlowxUuid())) {
            throw new BadRequestAlertException("Endpoint must be provided for rest node", WorkflowNode.class.getName(),
                    BadRequestErrorType.WORKFLOW_NOT_UPDATED);
        }
    }

    private void validateEndNode(UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
    }

    private void validateStartNode(UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
    }

    public void validate(WorkflowNodeType nodeType, UpdateWorkflowNodeReqDTO updateWorkflowNodeReqDTO) {
        validators.get(nodeType).accept(updateWorkflowNodeReqDTO);
    }
}
