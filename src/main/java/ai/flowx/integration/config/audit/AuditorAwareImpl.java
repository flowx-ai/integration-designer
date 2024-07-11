package ai.flowx.integration.config.audit;

import ai.flowx.commons.definitions.audit.domain.AuditorDetails;
import ai.flowx.security.security.ContextUser;
import ai.flowx.security.security.SecurityUtils;
import lombok.SneakyThrows;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<AuditorDetails> {

    @SneakyThrows
    @Override
    public Optional<AuditorDetails> getCurrentAuditor() {
        ContextUser contextUser = SecurityUtils.getContextUser();
        return Optional.of(new AuditorDetails(contextUser.getFirstName(), contextUser.getLastName(), contextUser.getUsername()));
    }
}