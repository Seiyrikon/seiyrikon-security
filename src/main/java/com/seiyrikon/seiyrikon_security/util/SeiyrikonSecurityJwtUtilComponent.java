package com.seiyrikon.seiyrikon_security.util;

import com.seiyrikon.seiyrikon_security.configuration.SeiyrikonSecurityTokenConfiguration;
import com.seiyrikon.seiyrikon_security.constant.SeiyrikonSecurityConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class SeiyrikonSecurityJwtUtilComponent {

    private final SeiyrikonSecurityTokenConfiguration securityTokenConfiguration;

    public String getUserId(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    public Date getJwtIssuedAt(String jwt) {
        return extractClaim(jwt, Claims::getIssuedAt);
    }

    public Date getJwtExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwt);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(securityTokenConfiguration.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
