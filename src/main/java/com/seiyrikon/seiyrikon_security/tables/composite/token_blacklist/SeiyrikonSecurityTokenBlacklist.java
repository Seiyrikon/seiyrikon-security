package com.seiyrikon.seiyrikon_security.tables.composite.token_blacklist;

import com.seiyrikon.seiyrikon_security.tables.composite.token_blacklist.id.SeiyrikonSecurityTokenBlacklistId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@NoArgsConstructor
public class SeiyrikonSecurityTokenBlacklist {

    @EmbeddedId
    private SeiyrikonSecurityTokenBlacklistId id;

    public SeiyrikonSecurityTokenBlacklist(String userId, String token) {
        this.id = new SeiyrikonSecurityTokenBlacklistId();
        this.id.setUserId(userId);
        this.id.setToken(token);
    }
}
