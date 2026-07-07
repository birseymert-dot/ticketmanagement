package com.ticketmanagement.exception;

/** 404 - Kaynak bulunamadi. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
