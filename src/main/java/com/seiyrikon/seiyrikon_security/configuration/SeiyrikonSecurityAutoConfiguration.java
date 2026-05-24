package com.seiyrikon.seiyrikon_security.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@EnableConfigurationProperties({
        SeiyrikonSecurityTokenConfiguration.class,
        SeiyrikonSecurityTableProperties.class
})
public class SeiyrikonSecurityAutoConfiguration {

    @Bean
    public SeiyrikonSecurityStartUpCheck startUpCheck(SeiyrikonSecurityTokenConfiguration securityTokenConfiguration) {
        return new  SeiyrikonSecurityStartUpCheck(securityTokenConfiguration);
    }

    @Bean
    public SeiyrikonSecurityTableInitializer tableInitializer(
            JdbcTemplate jdbcTemplate,
            SeiyrikonSecurityTableProperties properties
    ) {
        return new SeiyrikonSecurityTableInitializer(jdbcTemplate, properties);
    }
}
