package com.seiyrikon.seiyrikon_security.exception;

import lombok.Getter;

@Getter
public class SeiyrikonSecurityDefaultException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public SeiyrikonSecurityDefaultException(String errorCode, String errorMessage) {
        super(errorMessage);

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
