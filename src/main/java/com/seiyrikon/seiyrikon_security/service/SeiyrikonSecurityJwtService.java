package com.seiyrikon.seiyrikon_security.service;

public interface SeiyrikonSecurityJwtService {

    public String extractUsername(String token);

    public String extractUserId(String token);
}
