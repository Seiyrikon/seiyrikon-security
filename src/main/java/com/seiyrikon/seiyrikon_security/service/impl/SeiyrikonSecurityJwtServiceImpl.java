package com.seiyrikon.seiyrikon_security.service.impl;

import com.seiyrikon.seiyrikon_security.util.SeiyrikonSecurityJwtUtilComponent;
import com.seiyrikon.seiyrikon_security.service.SeiyrikonSecurityJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeiyrikonSecurityJwtServiceImpl implements SeiyrikonSecurityJwtService {

    private final SeiyrikonSecurityJwtUtilComponent seiyrikonSecurityJwtUtilComponent;

    @Override
    public String extractUsername(String token) {
        return "";
    }

    @Override
    public String extractUserId(String token) {
        return "";
    }
}
