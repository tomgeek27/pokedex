package com.tommasoamadori.pokedex.dto.response.funtranslations;

import com.tommasoamadori.pokedex.dto.response.funtranslations.model.TranslationContentModel;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record FunTranslationsResponse(TranslationContentModel contents) {}
