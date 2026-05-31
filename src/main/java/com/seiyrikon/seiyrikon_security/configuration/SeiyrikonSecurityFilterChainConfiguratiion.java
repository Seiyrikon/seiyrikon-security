package com.seiyrikon.seiyrikon_security.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SeiyrikonSecurityFilterChainConfiguratiion {

    private final SeiyrikonSecurityJwtAuthenticationFilter seiyrikonSecurityJwtAuthenticationFilter;
    private final SeiyrikonSecurityFilterChainProperties seiyrikonSecurityFilterChainProperties;

    @Bean
    public SecurityFilterChain springSecurityFilterChain(HttpSecurity http) throws Exception {

        // DEBUG - remove after fixing
        System.out.println("Whitelisted paths: " +
                Arrays.toString(seiyrikonSecurityFilterChainProperties.getWhitelistedPaths()));

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers(seiyrikonSecurityFilterChainProperties.getWhitelistedPaths()).permitAll().anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(seiyrikonSecurityJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
