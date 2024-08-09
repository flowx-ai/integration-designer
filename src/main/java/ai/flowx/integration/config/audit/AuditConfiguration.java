package ai.flowx.integration.config.audit;

import ai.flowx.commons.definitions.audit.domain.AuditorDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfiguration {

    @Bean
    public AuditorAware<AuditorDetails> auditorProvider() {
        return new AuditorAwareImpl();
    }

}
