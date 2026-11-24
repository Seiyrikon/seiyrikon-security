package com.seiyrikon.seiyrikon_security.util;

import com.seiyrikon.seiyrikon_security.repository.SeiyrikonSecurityTokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class SeiyrikonSecurityTokenCleanupScheduler {

    private final SeiyrikonSecurityTokenBlacklistRepository blacklistRepository;

    @Scheduled(cron = "${seiyrikon-security.blacklist-cleanup-cron:0 0 * * * *}")
    public void cleanBlacklistedTokens() {
        int deleted = blacklistRepository.cleanBlacklistedTokens();
        if (deleted > 0) {
            log.info("Seiyrikon Security: removed {} expired blacklisted tokens.", deleted);
        }
    }
}
