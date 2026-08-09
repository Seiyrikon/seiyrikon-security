package com.seiyrikon.seiyrikon_security.repository;

import com.seiyrikon.seiyrikon_security.configuration.SeiyrikonSecurityJwtConfiguration;
import com.seiyrikon.seiyrikon_security.configuration.SeiyrikonSecurityTableProperties;
import com.seiyrikon.seiyrikon_security.exception.auth.SeiyrikonSecurityAuthenticationException;
import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@RequiredArgsConstructor
public class SeiyrikonSecurityTokenBlacklistRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent;
    private final SeiyrikonSecurityJwtConfiguration seiyrikonSecurityJwtConfiguration;
    private final SeiyrikonSecurityTableProperties properties;

    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw SeiyrikonSecurityAuthenticationException.authHeaderIsNullOrNotBearer();
        }

        String token = authHeader.substring(seiyrikonSecurityJwtConfiguration.getPrefix().length());

        Date expiry = seiyrikonSecurityJwtUtilComponent.getJwtExpiration(token);

        String sql = String.format(
                "INSERT IGNORE INTO %s (%s, %s) VALUES (?, ?)",
                properties.getTokenBlacklistTable(),
                properties.getTokenBlacklistTokenColumn(),
                properties.getTokenBlacklistExpiryColumn()
        );

        jdbcTemplate.update(sql, token, expiry);
    }

    public boolean isTokenBlacklisted(String token) {
        String sql = String.format(
                "SELECT COUNT(*) FROM %s WHERE %s = ?",
                properties.getTokenBlacklistTable(),
                properties.getTokenBlacklistTokenColumn()
        );
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, token);
        return count != null && count > 0;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredTokens() {
        String sql = String.format(
                "DELETE FROM %s WHERE %s < NOW()",
                properties.getTokenBlacklistTable(),
                properties.getTokenBlacklistTokenColumn()
        );
        jdbcTemplate.update(sql);
    }
}
