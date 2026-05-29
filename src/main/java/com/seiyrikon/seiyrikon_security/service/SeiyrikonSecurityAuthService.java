package com.seiyrikon.seiyrikon_security.service;

import com.seiyrikon.seiyrikon_security.domain.SeiyrikonSecurityAuthResponse;

public interface SeiyrikonSecurityAuthService {

    public SeiyrikonSecurityAuthResponse authenticate(String identifier, String rawPassword);
}
