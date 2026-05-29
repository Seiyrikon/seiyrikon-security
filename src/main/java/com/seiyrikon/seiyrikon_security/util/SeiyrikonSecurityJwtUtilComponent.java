package com.seiyrikon.seiyrikon_security.util;

import com.seiyrikon.seiyrikon_security.configuration.SeiyrikonSecurityTokenConfiguration;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class SeiyrikonSecurityJwtUtilComponent {

    private final SeiyrikonSecurityTokenConfiguration securityTokenConfiguration;

    public String generateToken(Map<String, Object> extraClaims, Object userId) {
        Instant now = Instant.now();
        Instant jwtExpiration = now.plus(30, ChronoUnit.MINUTES);

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserId(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
    }

    public Date getJwtIssuedAt(String jwt) {
        return extractClaim(jwt, Claims::getIssuedAt);
    }

    public Date getJwtExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    public List<String> getRoles(String jwt) {
        Claims claims = extractAllClaims(jwt);

        Object roles = claims.get("roles");

        if(roles instanceof List<?>) {
            return ((List<?>) roles)
                    .stream()
                    .map(Object::toString)
                    .toList();
        }

        return Collections.emptyList();
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
