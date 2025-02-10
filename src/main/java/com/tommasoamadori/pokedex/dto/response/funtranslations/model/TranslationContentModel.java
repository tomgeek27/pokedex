package com.tommasoamadori.pokedex.dto.response.funtranslations.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TranslationContentModel(String translated) { }
