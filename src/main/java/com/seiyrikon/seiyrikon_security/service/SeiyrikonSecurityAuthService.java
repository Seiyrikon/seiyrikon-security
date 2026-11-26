package com.seiyrikon.seiyrikon_security.service;

import com.seiyrikon.seiyrikon_security.constant.AppConstants;
import com.seiyrikon.seiyrikon_security.model.SeiyrikonSecurityAuthResponse;
import com.seiyrikon.seiyrikon_security.model.SeiyrikonSecurityUser;
import com.seiyrikon.seiyrikon_security.exception.auth.SeiyrikonSecurityAuthenticationException;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityTokenBlacklistRepository;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityTokenSessionRepository;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityUserRoleRepository;
import com.seiyrikon.seiyrikon_security.tables.composite.token_blacklist.SeiyrikonSecurityTokenBlacklist;
import com.seiyrikon.seiyrikon_security.tables.composite.token_session.SeiyrikonSecurityTokenSession;
import com.seiyrikon.seiyrikon_security.tables.composite.user_role.SeiyrikonSecurityUserRole;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SeiyrikonSecurityAuthService {

    private final SeiyrikonSecurityAuthProvider seiyrikonSecurityAuthProvider;
    private final SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent;
    private final SeiyrikonSecurityUserRoleRepository seiyrikonSecurityUserRoleRepository;
    private final SeiyrikonSecurityTokenSessionRepository seiyrikonSecurityTokenSessionRepository;
    private final SeiyrikonSecurityTokenBlacklistRepository seiyrikonSecurityTokenBlacklistRepository;

    public SeiyrikonSecurityAuthResponse authenticate(String identifier, String rawPassword) {

        SeiyrikonSecurityUser user = seiyrikonSecurityAuthProvider.findUser(identifier);
        if (user == null) {
            throw SeiyrikonSecurityAuthenticationException.userNotFound();
        }

        if (!seiyrikonSecurityAuthProvider.verifyPassword(rawPassword, user.getHashedPassword())) {
            throw SeiyrikonSecurityAuthenticationException.invalidCredentials();
        }

        List<String> roles = seiyrikonSecurityUserRoleRepository.findRoleNamesByUserId(user.getUserId());
        if (roles.isEmpty()) {
            throw SeiyrikonSecurityAuthenticationException.userLacksRoles();
        }

        List<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("username", user.getUsername());
        extraClaims.put("roles", roles);

        String accessToken = seiyrikonSecurityJwtUtilComponent.generateToken(extraClaims, user.getUserId());
        String refreshToken = seiyrikonSecurityJwtUtilComponent.generateRefreshToken(extraClaims, user.getUserId());

        SeiyrikonSecurityPrincipal principal = SeiyrikonSecurityPrincipal.builder()
                .userId(user.getUserId())
                .authorities(authorities)
                .build();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        SeiyrikonSecurityTokenSession tokenSession =
                seiyrikonSecurityTokenSessionRepository
                        .findById(new SeiyrikonSecurityTokenSession(user.getUserId())
                                .getId()).orElse(null);

        if(tokenSession == null) {
            tokenSession = new SeiyrikonSecurityTokenSession(user.getUserId());
        }
        tokenSession.setToken(accessToken);
        tokenSession.setRefreshToken(refreshToken);
        seiyrikonSecurityTokenSessionRepository.save(tokenSession);

        return SeiyrikonSecurityAuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    public SeiyrikonSecurityAuthResponse register(String identifier) {

        SeiyrikonSecurityUser user = seiyrikonSecurityAuthProvider.findUser(identifier);
        if (user == null) {
            throw SeiyrikonSecurityAuthenticationException.userNotFound();
        }

        seiyrikonSecurityUserRoleRepository.save(new SeiyrikonSecurityUserRole(user.getUserId(), AppConstants.USER_ROLE));

        List<String> roles = seiyrikonSecurityUserRoleRepository.findRoleNamesByUserId(user.getUserId());
        if (roles.isEmpty()) {
            throw SeiyrikonSecurityAuthenticationException.userLacksRoles();
        }

        List<GrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("username", user.getUsername());
        extraClaims.put("roles", roles);

        String accessToken = seiyrikonSecurityJwtUtilComponent.generateToken(extraClaims, user.getUserId());
        String refreshToken = seiyrikonSecurityJwtUtilComponent.generateRefreshToken(extraClaims, user.getUserId());

        SeiyrikonSecurityPrincipal principal = SeiyrikonSecurityPrincipal.builder()
                .userId(user.getUserId())
                .authorities(authorities)
                .build();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        SeiyrikonSecurityTokenSession tokenSession =
                new SeiyrikonSecurityTokenSession(user.getUserId());
        tokenSession.setRefreshToken(refreshToken);

        seiyrikonSecurityTokenSessionRepository.save(tokenSession);

        return SeiyrikonSecurityAuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    public boolean logout() {
        String userId = SeiyrikonSecurityPrincipal.getCurrentUserId().toString();

        SeiyrikonSecurityTokenSession defaultTokenSession = new SeiyrikonSecurityTokenSession(userId);
        defaultTokenSession.setToken(AppConstants.TO_CLEAN);

        SeiyrikonSecurityTokenSession tokenSession = seiyrikonSecurityTokenSessionRepository
                .findById(new SeiyrikonSecurityTokenSession(userId)
                .getId())
                .orElse(defaultTokenSession);

        SeiyrikonSecurityTokenBlacklist tokenBlacklist =
                new SeiyrikonSecurityTokenBlacklist(userId);
        tokenBlacklist.setToken(tokenSession.getToken());

        SeiyrikonSecurityTokenBlacklist blacklistedToken = seiyrikonSecurityTokenBlacklistRepository.save(tokenBlacklist);
        seiyrikonSecurityTokenSessionRepository.deleteById(tokenSession.getId());

        return seiyrikonSecurityTokenBlacklistRepository.existsById(blacklistedToken.getId());
    }
}
