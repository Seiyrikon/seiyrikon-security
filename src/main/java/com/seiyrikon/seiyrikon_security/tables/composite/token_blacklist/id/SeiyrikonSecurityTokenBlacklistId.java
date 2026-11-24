package com.seiyrikon.seiyrikon_security.tables.composite.token_blacklist.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class SeiyrikonSecurityTokenBlacklistId {
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "token", length = 1024)
    private String token;
}
