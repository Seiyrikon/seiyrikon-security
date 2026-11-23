package com.seiyrikon.seiyrikon_security.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "token_blacklist")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SeiyrikonSecurityTokenBlacklist {

    @Id
    @Column(name = "token", length = 1024)
    private String token;

    @Column(name = "expiry", nullable = false)
    private Instant expiry;
}
