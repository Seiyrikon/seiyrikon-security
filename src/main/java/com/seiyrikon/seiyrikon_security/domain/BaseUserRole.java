package com.seiyrikon.seiyrikon_security.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class BaseUserRole <U, R>{

    @Column(nullable = false)
    private U userId;

    @Column(nullable = false)
    private R roleId;
}
