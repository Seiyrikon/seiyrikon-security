package com.seiyrikon.seiyrikon_security.configuration;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SeiyrikonSecurityJwtConfiguration {
    private String header = "Authorization";
    private String prefix = "Bearer ";

}
