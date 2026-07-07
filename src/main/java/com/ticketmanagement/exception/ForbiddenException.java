package com.ticketmanagement.exception;

/** 403 - Yetkisiz erisim (is kurali ihlali: sahiplik / rol kontrolu). */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
