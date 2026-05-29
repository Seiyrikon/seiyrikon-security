package com.seiyrikon.seiyrikon_security.configuration;

import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityUserRoleRepository;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityAuthProvider;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityAuthServiceImpl;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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

    @Bean
    public SeiyrikonSecurityJwtUtilComponent jwtUtilComponent(SeiyrikonSecurityTokenConfiguration securityTokenConfiguration) {
        return new  SeiyrikonSecurityJwtUtilComponent(securityTokenConfiguration);
    }

    @Bean
    @ConditionalOnBean(SeiyrikonSecurityAuthProvider.class)
    public SeiyrikonSecurityAuthServiceImpl seiyrikonSecurityAuthService(
            SeiyrikonSecurityAuthProvider seiyrikonSecurityAuthProvider,
            SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent,
            SeiyrikonSecurityUserRoleRepository seiyrikonSecurityUserRoleRepository
    ) {
        return new SeiyrikonSecurityAuthServiceImpl(seiyrikonSecurityAuthProvider, seiyrikonSecurityJwtUtilComponent, seiyrikonSecurityUserRoleRepository);
    }
}
