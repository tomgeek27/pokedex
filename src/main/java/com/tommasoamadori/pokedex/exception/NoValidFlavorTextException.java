package com.tommasoamadori.pokedex.exception;

public class NoValidFlavorTextException extends RuntimeException {
    public NoValidFlavorTextException() {
        super("No valid flavor text found");
    }
}
