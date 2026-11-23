package com.seiyrikon.seiyrikon_security.tables;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_role")
@Getter
@Setter
@NoArgsConstructor
public class SeiyrikonSecurityUserRole {
    @EmbeddedId
    private SeiyrikonSecurityUserRoleId id;
}
