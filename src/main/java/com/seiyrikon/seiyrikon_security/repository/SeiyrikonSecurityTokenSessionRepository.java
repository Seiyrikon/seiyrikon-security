package com.seiyrikon.seiyrikon_security.repository;

import com.seiyrikon.seiyrikon_security.tables.composite.token_session.SeiyrikonSecurityTokenSession;
import com.seiyrikon.seiyrikon_security.tables.composite.token_session.id.SeiyrikonSecurityTokenSessionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeiyrikonSecurityTokenSessionRepository extends JpaRepository<SeiyrikonSecurityTokenSession, SeiyrikonSecurityTokenSessionId> {
}
