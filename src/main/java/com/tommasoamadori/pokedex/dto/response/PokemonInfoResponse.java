package com.tommasoamadori.pokedex.dto.response;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Serdeable
public class PokemonInfoResponse {
    private String name;
    private String description;
    private String habitat;
    private Boolean isLegendary;
}

