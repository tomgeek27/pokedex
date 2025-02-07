package com.tommasoamadori.pokedex.dto.response.pokeapi.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record HabitatModel(String name) {}
