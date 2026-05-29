package com.seiyrikon.seiyrikon_security.exception.auth;

import com.seiyrikon.seiyrikon_security.constant.SeiyrikonSecurityExceptionConstant;
import com.seiyrikon.seiyrikon_security.exception.SeiyrikonSecurityDefaultException;

public class SeiyrikonSecurityAuthenticationException extends SeiyrikonSecurityDefaultException {

    public SeiyrikonSecurityAuthenticationException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public static SeiyrikonSecurityAuthenticationException userNotFound() {
        return new SeiyrikonSecurityAuthenticationException(
                SeiyrikonSecurityExceptionConstant.AUTH_01,
                SeiyrikonSecurityExceptionConstant.ERR_MESSAGE_01
        );
    }

    public static SeiyrikonSecurityAuthenticationException invalidCredentials() {
        return new SeiyrikonSecurityAuthenticationException(
                SeiyrikonSecurityExceptionConstant.AUTH_02,
                SeiyrikonSecurityExceptionConstant.ERR_MESSAGE_02
        );
    }

    public static SeiyrikonSecurityAuthenticationException userLacksRoles() {
        return new SeiyrikonSecurityAuthenticationException(
                SeiyrikonSecurityExceptionConstant.AUTH_03,
                SeiyrikonSecurityExceptionConstant.ERR_MESSAGE_03
        );
    }
}
