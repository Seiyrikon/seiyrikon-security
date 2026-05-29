package com.seiyrikon.seiyrikon_security.service.impl;

import com.seiyrikon.seiyrikon_security.domain.SeiyrikonSecurityAuthResponse;
import com.seiyrikon.seiyrikon_security.domain.SeiyrikonSecurityUser;
import com.seiyrikon.seiyrikon_security.exception.auth.SeiyrikonSecurityAuthenticationException;
import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityUserRoleRepository;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityAuthProvider;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityAuthService;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SeiyrikonSecurityAuthServiceImpl implements SeiyrikonSecurityAuthService {

    private final SeiyrikonSecurityAuthProvider seiyrikonSecurityAuthProvider;
    private final SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent;
    private final SeiyrikonSecurityUserRoleRepository seiyrikonSecurityUserRoleRepository;

    @Override
    public SeiyrikonSecurityAuthResponse authenticate(String identifier, String rawPassword) {

        SeiyrikonSecurityUser user = seiyrikonSecurityAuthProvider.findUser(identifier);
        if (user == null) {
            throw SeiyrikonSecurityAuthenticationException.userNotFound();
        }

        if (!seiyrikonSecurityAuthProvider.verifyPassword(rawPassword, user.getHashedPassword())) {
            throw SeiyrikonSecurityAuthenticationException.invalidCredentials();
        }

        List<String> roles = seiyrikonSecurityUserRoleRepository.getUserRoles(user.getUserId());
        if (roles.isEmpty()) {
            throw SeiyrikonSecurityAuthenticationException.userLacksRoles();
        }

        List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roles);

        String accessToken = seiyrikonSecurityJwtUtilComponent.generateToken(extraClaims, user.getUserId());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        user.getUserId(),
                        null,
                        authorities
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return SeiyrikonSecurityAuthResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
