package com.seiyrikon.seiyrikon_security.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeiyrikonSecurityUser {
    private Object userId;
    private String username;
    private String hashedPassword;
}
