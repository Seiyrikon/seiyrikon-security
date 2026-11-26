package com.seiyrikon.seiyrikon_security.configuration;

import com.seiyrikon.seiyrikon_security.model.SeiyrikonSecurityUser;
import com.seiyrikon.seiyrikon_security.exception.auth.SeiyrikonSecurityAuthenticationException;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityTokenBlacklistRepository;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityTokenSessionRepository;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityAuthProvider;
import com.seiyrikon.seiyrikon_security.tables.composite.token_blacklist.SeiyrikonSecurityTokenBlacklist;
import com.seiyrikon.seiyrikon_security.tables.composite.token_session.SeiyrikonSecurityTokenSession;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityPrincipal;
import com.seiyrikon.seiyrikon_security.validator.jwt.JwtValidatorComponent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeiyrikonSecurityJwtAuthenticationFilter extends OncePerRequestFilter {

    private final SeiyrikonSecurityJwtConfiguration seiyrikonSecurityJwtConfiguration;
    private final SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent;
    private final SeiyrikonSecurityAuthProvider seiyrikonSecurityAuthProvider;
    private final SeiyrikonSecurityTokenBlacklistRepository seiyrikonSecurityTokenBlacklistRepository;
    private final JwtValidatorComponent jwtValidatorComponent;
    private final SeiyrikonSecurityTokenSessionRepository seiyrikonSecurityTokenSessionRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> extraClaims = new HashMap<>();
        String header = request.getHeader(seiyrikonSecurityJwtConfiguration.getHeader());

        if(header == null || !header.startsWith(seiyrikonSecurityJwtConfiguration.getPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = header.substring(seiyrikonSecurityJwtConfiguration.getPrefix().length());

        String username = seiyrikonSecurityJwtUtilComponent.getUsername(jwt);

        SeiyrikonSecurityUser user = seiyrikonSecurityAuthProvider.findUser(username);

        if(user == null) {
            throw SeiyrikonSecurityAuthenticationException.userNotFound();
        }

        SeiyrikonSecurityTokenBlacklist tokenBlacklist =
                seiyrikonSecurityTokenBlacklistRepository
                        .findById(new SeiyrikonSecurityTokenBlacklist(user.getUserId()).getId())
                        .orElse(null);

        if(tokenBlacklist != null) {
            if(jwt.equals(tokenBlacklist.getToken())) {
                throw SeiyrikonSecurityAuthenticationException.blackListedToken();
            }
        }

        if(!jwtValidatorComponent.validToken(jwt)) {
            SeiyrikonSecurityTokenSession currentTokenSession =
                    seiyrikonSecurityTokenSessionRepository.findById(
                            new SeiyrikonSecurityTokenSession(user.getUserId()).getId())
                            .orElseThrow(SeiyrikonSecurityAuthenticationException::unauthenticatedUser);

            if(jwtValidatorComponent.validToken(currentTokenSession.getRefreshToken())) {
                List<String> roles = seiyrikonSecurityJwtUtilComponent.getRoles(currentTokenSession.getRefreshToken());
                extraClaims.put("username", username);
                extraClaims.put("roles", roles);

                jwt = seiyrikonSecurityJwtUtilComponent
                        .generateToken(extraClaims, user.getUserId());

                String refreshToken = seiyrikonSecurityJwtUtilComponent
                        .generateRefreshToken(extraClaims, user.getUserId());

                currentTokenSession.setId(new SeiyrikonSecurityTokenSession(user.getUserId()).getId());
                currentTokenSession.setToken(jwt);
                currentTokenSession.setRefreshToken(refreshToken);
                seiyrikonSecurityTokenSessionRepository.save(currentTokenSession);
            } else {
                throw SeiyrikonSecurityAuthenticationException.reAuthenticate();
            }
        }

        if(SecurityContextHolder.getContext().getAuthentication() == null) {
            List<String> roles = seiyrikonSecurityJwtUtilComponent.getRoles(jwt);

            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            SeiyrikonSecurityPrincipal principal = SeiyrikonSecurityPrincipal.builder()
                    .userId(user.getUserId())
                    .authorities(authorities)
                    .build();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
