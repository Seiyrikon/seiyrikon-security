package com.seiyrikon.seiyrikon_security.tables.composite.token_session;

import com.seiyrikon.seiyrikon_security.tables.composite.token_session.id.SeiyrikonSecurityTokenSessionId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_session")
@NoArgsConstructor
@Getter
@Setter
public class SeiyrikonSecurityTokenSession {

    @EmbeddedId
    private SeiyrikonSecurityTokenSessionId id;

    @Column(name = "refreshToken", length = 1024)
    private String refreshToken;

    public SeiyrikonSecurityTokenSession(String userId, String token) {
        this.id = new SeiyrikonSecurityTokenSessionId();
        this.id.setUserId(userId);
        this.id.setToken(token);
    }
}
