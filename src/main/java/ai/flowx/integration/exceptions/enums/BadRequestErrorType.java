package ai.flowx.integration.exceptions.enums;

import ai.flowx.commons.errors.I18NMessage;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The BadRequestErrorType enumeration.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@AllArgsConstructor
public enum BadRequestErrorType implements I18NMessage {

    CODE_EXISTS("error.system.codeExists"),
    SYSTEM_NOT_FOUND("error.system.notFound"),
    SYSTEM_NOT_UPDATED("error.system.notUpdated"),
    VARIABLE_KEY_EXISTS("error.system.variable.key.exists"),
    VARIABLE_NOT_FOUND("error.system.variable.notFound"),
    ENDPOINT_NOT_FOUND("error.endpoint.notFound"),
    ENDPOINT_NOT_UPDATED("error.endpoint.notUpdated"),
    PARAMETER_NOT_FOUND("error.endpoint.parameter.notFound"),
    ID_NOT_NULL("error.generic.id.notNull"),
    RESPONSE_NOT_FOUND("error.endpoint.response.notFound"),
    WORKFLOW_NOT_FOUND("error.workflow.notFound"),
    WORKFLOW_NAME_REQUIRED("error.workflow.nameRequired"),
    WORKFLOW_NAME_EXISTS("error.workflow.nameExists"),
    INVALID_ID("error.invalid.id"),
    WORKFLOW_NOT_UPDATED("error.workflow.notUpdated"),
    INVALID_NODE_TYPE("error.invalid.nodeType"),
    WORKFLOW_NODE_NAME_REQUIRED("error.workflow.node.nameRequired"),
    NODES_NOT_UPDATED("error.workflow.nodes.notUpdated"),
    NODE_NOT_UPDATED("error.workflow.node.notUpdated"),
    WORKFLOW_NODE_NOT_FOUND("error.workflow.node.notFound"),

    ;

    private String errorKey;
}
