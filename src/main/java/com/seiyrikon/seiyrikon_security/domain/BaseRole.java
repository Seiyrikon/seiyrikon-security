package com.seiyrikon.seiyrikon_security.domain;

import jakarta.persistence.*;
import lombok.Data;

@MappedSuperclass
@Data
public abstract class BaseRole<T> {

    @Id
    private T id;

    @Column(unique = true, length = 50)
    private String roleName;
}
