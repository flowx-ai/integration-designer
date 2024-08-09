package ai.flowx.integration.advancing.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "advancing")
@Getter
@Setter
public class AdvancingProperties {

    @Data
    public static class DatasourceProperties {
        private String jdbcUrl;
        private String driverClassName;
        private String username;
        private String password;
    }

    private int threads;
    private int pickingBatchSize = 1;
    private long pickingPauseMillis = 500;
    private long cooldownAfterSeconds = 2 * 60;
    private DatasourceProperties datasource;

}
