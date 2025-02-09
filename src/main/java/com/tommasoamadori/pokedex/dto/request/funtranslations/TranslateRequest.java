package com.tommasoamadori.pokedex.dto.request.funtranslations;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TranslateRequest(String text) { }
