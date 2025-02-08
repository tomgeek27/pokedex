package com.tommasoamadori.pokedex.exception;

public class UnexpectedResponseBodyException extends RuntimeException {
    public UnexpectedResponseBodyException(String serviceName) {
        super("Unexpected response body from service %s".formatted(serviceName));
    }
}
