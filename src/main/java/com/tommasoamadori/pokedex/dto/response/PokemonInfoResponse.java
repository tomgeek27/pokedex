package com.tommasoamadori.pokedex.dto.response;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;

@Builder
@Serdeable
public record PokemonInfoResponse(
     String name,
     String description,
     String habitat,
     Boolean isLegendary
) {}
