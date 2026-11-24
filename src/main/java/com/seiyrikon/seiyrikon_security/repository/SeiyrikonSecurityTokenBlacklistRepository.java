package com.seiyrikon.seiyrikon_security.repository;

import com.seiyrikon.seiyrikon_security.tables.composite.token_blacklist.SeiyrikonSecurityTokenBlacklist;
import com.seiyrikon.seiyrikon_security.tables.composite.token_blacklist.id.SeiyrikonSecurityTokenBlacklistId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SeiyrikonSecurityTokenBlacklistRepository extends JpaRepository<SeiyrikonSecurityTokenBlacklist, SeiyrikonSecurityTokenBlacklistId> {

    @Modifying
    @Transactional
    @Query("delete from SeiyrikonSecurityTokenBlacklist t")
    int cleanBlacklistedTokens();
}
