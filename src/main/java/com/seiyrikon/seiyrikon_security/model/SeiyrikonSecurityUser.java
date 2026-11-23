package com.seiyrikon.seiyrikon_security.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeiyrikonSecurityUser {
    private String userId;
    private String username;
    private String hashedPassword;
}
