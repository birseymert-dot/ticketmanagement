package com.ticketmanagement.exception;

/** 400 - Gecersiz istek (ornegin gecersiz status gecisi). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
