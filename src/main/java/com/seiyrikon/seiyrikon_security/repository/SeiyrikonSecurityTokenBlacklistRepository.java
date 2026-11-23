package com.seiyrikon.seiyrikon_security.repository;

import com.seiyrikon.seiyrikon_security.tables.SeiyrikonSecurityTokenBlacklist;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SeiyrikonSecurityTokenBlacklistRepository extends JpaRepository<SeiyrikonSecurityTokenBlacklist, String> {

    @Modifying
    @Transactional
    @Query("delete from SeiyrikonSecurityTokenBlacklist t where t.expiry < :now")
    int deleteExpired(@Param("now") Instant now);
}
