package com.seiyrikon.seiyrikon_security.configuration;

import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityTokenBlacklistRepository;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityTokenSessionRepository;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityUserRoleRepository;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityAuthProvider;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityAuthService;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityTokenCleanupScheduler;
import com.seiyrikon.seiyrikon_security.validator.jwt.JwtValidatorComponent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@AutoConfigurationPackage(basePackages = "com.seiyrikon.seiyrikon_security")
@EnableScheduling
@Import(SeiyrikonSecurityFilterChainConfiguratiion.class)
@EnableConfigurationProperties({
        SeiyrikonSecurityTokenConfiguration.class,
        SeiyrikonSecurityFilterChainProperties.class
})
public class SeiyrikonSecurityAutoConfiguration {

    @Bean
    public SeiyrikonSecurityStartUpCheck startUpCheck(SeiyrikonSecurityTokenConfiguration securityTokenConfiguration) {
        return new  SeiyrikonSecurityStartUpCheck(securityTokenConfiguration);
    }

    @Bean
    public SeiyrikonSecurityTokenCleanupScheduler tokenCleanupScheduler(
            SeiyrikonSecurityTokenBlacklistRepository blacklistRepository) {
        return new SeiyrikonSecurityTokenCleanupScheduler(blacklistRepository);
    }

    @Bean
    public SeiyrikonSecurityJwtUtilComponent jwtUtilComponent(SeiyrikonSecurityTokenConfiguration securityTokenConfiguration) {
        return new SeiyrikonSecurityJwtUtilComponent(securityTokenConfiguration);
    }

    @Bean
    public JwtValidatorComponent jwtValidatorComponent(SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent) {
        return new JwtValidatorComponent(seiyrikonSecurityJwtUtilComponent);
    }

    @Bean
    @ConditionalOnBean(SeiyrikonSecurityAuthProvider.class)
    public SeiyrikonSecurityAuthService seiyrikonSecurityAuthService(
            SeiyrikonSecurityAuthProvider seiyrikonSecurityAuthProvider,
            SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent,
            SeiyrikonSecurityUserRoleRepository seiyrikonSecurityUserRoleRepository,
            SeiyrikonSecurityTokenSessionRepository seiyrikonSecurityTokenSessionRepository,
            SeiyrikonSecurityTokenBlacklistRepository seiyrikonSecurityTokenBlacklistRepository
    ) {
        return new SeiyrikonSecurityAuthService(
                seiyrikonSecurityAuthProvider,
                seiyrikonSecurityJwtUtilComponent,
                seiyrikonSecurityUserRoleRepository,
                seiyrikonSecurityTokenSessionRepository,
                seiyrikonSecurityTokenBlacklistRepository);
    }

    @Bean
    public SeiyrikonSecurityJwtAuthenticationFilter seiyrikonSecurityJwtAuthenticationFilter(
            SeiyrikonSecurityJwtConfiguration seiyrikonSecurityJwtConfiguration,
            SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent,
            SeiyrikonSecurityAuthProvider seiyrikonSecurityAuthProvider,
            SeiyrikonSecurityTokenBlacklistRepository seiyrikonSecurityTokenBlacklistRepository,
            JwtValidatorComponent jwtValidatorComponent,
            SeiyrikonSecurityTokenSessionRepository seiyrikonSecurityTokenSessionRepository
    ) {
        return new SeiyrikonSecurityJwtAuthenticationFilter(
                seiyrikonSecurityJwtConfiguration,
                seiyrikonSecurityJwtUtilComponent,
                seiyrikonSecurityAuthProvider,
                seiyrikonSecurityTokenBlacklistRepository,
                jwtValidatorComponent,
                seiyrikonSecurityTokenSessionRepository);
    }

    @Bean
    public SeiyrikonSecurityJwtConfiguration securityJwtConfiguration() {
        return new SeiyrikonSecurityJwtConfiguration();
    }
}
