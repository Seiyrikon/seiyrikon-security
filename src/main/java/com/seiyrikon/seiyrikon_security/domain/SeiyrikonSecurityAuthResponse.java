package com.seiyrikon.seiyrikon_security.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeiyrikonSecurityAuthResponse {

    private String accessToken;
}
