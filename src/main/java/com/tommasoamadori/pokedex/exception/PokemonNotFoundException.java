package com.tommasoamadori.pokedex.exception;

public class PokemonNotFoundException extends RuntimeException {
    public PokemonNotFoundException(String name) {
        super("Pokemon '%s' not found".formatted(name));
    }
}
