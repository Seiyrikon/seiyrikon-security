package com.seiyrikon.seiyrikon_security.configuration;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SeiyrikonSecurityStartUpCheck {
    private final SeiyrikonSecurityTokenConfiguration securityTokenConfiguration;

    @PostConstruct
    public void check() {
        if(securityTokenConfiguration.getSecretKey() == null || securityTokenConfiguration.getSecretKey().isBlank()) {
            throw new IllegalStateException("JWT_SECRET_KEY is not set! Check your environment variables.");
        }
        System.out.println("JWT Secret Key loaded successfully.");
    }
}
