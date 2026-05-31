package com.seiyrikon.seiyrikon_security.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seiyrikon-security.whitelisted-paths")
@Data
public class SeiyrikonSecurityFilterChainProperties {
    private String[] whitelistedPaths;
}
