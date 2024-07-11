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
    ;

    private String errorKey;
}
