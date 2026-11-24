package com.seiyrikon.seiyrikon_security.validator.jwt;

import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtValidatorComponent {

    private final SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent;

    public boolean validToken(String jwt) {
        return seiyrikonSecurityJwtUtilComponent.getJwtIssuedAt(jwt).before(seiyrikonSecurityJwtUtilComponent.getJwtExpiration(jwt));
    }
}
