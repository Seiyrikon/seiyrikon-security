package com.seiyrikon.seiyrikon_security.tables.composite.user_role.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
public class SeiyrikonSecurityUserRoleId implements Serializable {

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "role_id", length = 25)
    private String roleId;
}