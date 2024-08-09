package ai.flowx.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.auth")
public class AuthConfig {
    private List<ClientConfig> clients;
    private String serverUrl;

    @Data
    public static class ClientConfig {
        private String realm;
        private String clientId;
        private String clientSecret;
    }
}