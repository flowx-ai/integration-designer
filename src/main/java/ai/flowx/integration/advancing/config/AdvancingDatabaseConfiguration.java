package ai.flowx.integration.advancing.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class AdvancingDatabaseConfiguration {

    @Bean
    @Qualifier("advancingDataSource")
    @ConfigurationProperties(prefix="advancing.datasource")
    public DataSource advancingDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("advancingJdbcTemplate")
    public JdbcTemplate advancingJdbcTemplate(@Qualifier("advancingDataSource") DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}
