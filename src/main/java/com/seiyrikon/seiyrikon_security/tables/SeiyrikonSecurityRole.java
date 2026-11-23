package com.seiyrikon.seiyrikon_security.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "role")
@NoArgsConstructor
@Getter
@Setter
public class SeiyrikonSecurityRole {

    @Id
    @Column(name = "id", length = 25)
    private String id;

    @Column(name = "name", nullable = false, unique = true, length = 25)
    private String name;
}
