package com.seiyrikon.seiyrikon_security.configuration;

import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeiyrikonSecurityJwtAuthenticationFilter extends OncePerRequestFilter {

    private final SeiyrikonSecurityJwtConfiguration seiyrikonSecurityJwtConfiguration;
    private final SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(seiyrikonSecurityJwtConfiguration.getHeader());

        if(header == null || !header.startsWith(seiyrikonSecurityJwtConfiguration.getPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = header.substring(seiyrikonSecurityJwtConfiguration.getPrefix().length());

        String userId = seiyrikonSecurityJwtUtilComponent.getUserId(jwt);

        if(userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<String> roles = seiyrikonSecurityJwtUtilComponent.getRoles(jwt);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, null, roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
