package com.seiyrikon.seiyrikon_security.configuration;

import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityUserRoleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SeiyrikonSecurityTableProperties.class)
public class SeiyrikonSecurityTableConfiguration {

    @Bean
    public SeiyrikonSecurityUserRoleRepository seiyrikonSecurityUserRoleRepository(EntityManager entityManager, SeiyrikonSecurityTableProperties properties) {
        return new SeiyrikonSecurityUserRoleRepository(entityManager, properties);
    }
}
