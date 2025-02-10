package com.tommasoamadori.pokedex.dto.response.pokeapi.model;

import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;

@Serdeable(naming = SnakeCaseStrategy.class)
public record FlavorTextModel(String flavorText, FlavorLanguageModel language) { }
