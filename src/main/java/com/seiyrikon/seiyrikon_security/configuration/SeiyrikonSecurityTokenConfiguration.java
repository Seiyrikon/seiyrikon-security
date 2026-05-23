package com.seiyrikon.seiyrikon_security.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "seiyrikon-security.jwt")
@Data
public class SeiyrikonSecurityTokenConfiguration {
    private String secretKey;
}
