package ai.flowx.integration.config.audit;

import ai.flowx.commons.definitions.audit.domain.AuditorDetails;
import ai.flowx.commons.definitions.audit.dtos.AuditorDetailsDTO;
import ai.flowx.security.security.ContextUser;
import ai.flowx.security.security.SecurityUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditUtils {
    public static AuditorDetails getAuditorDetails() {
        ContextUser contextUser = SecurityUtils.getContextUser();
        return new AuditorDetails(contextUser.getFirstName(), contextUser.getLastName(), contextUser.getUsername());
    }

    public static AuditorDetailsDTO getAuditorDetailsDTO() {
        return toAuditorDetailsDto(getAuditorDetails());
    }

    public static AuditorDetailsDTO toAuditorDetailsDto(AuditorDetails auditorDetails) {
        if (auditorDetails == null) {
            return null;
        }
        AuditorDetailsDTO dto = new AuditorDetailsDTO();
        dto.setFirstName(auditorDetails.getFirstName());
        dto.setLastName(auditorDetails.getLastName());
        dto.setUserName(auditorDetails.getUserName());
        return dto;
    }
}
