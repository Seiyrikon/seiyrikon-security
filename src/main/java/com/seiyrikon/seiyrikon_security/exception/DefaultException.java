package com.seiyrikon.seiyrikon_security.exception;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class DefaultException {
    private String errorCode;
    private String errorMessage;
}
