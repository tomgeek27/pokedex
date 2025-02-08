package com.tommasoamadori.pokedex.dto.response.pokeapi;

import com.tommasoamadori.pokedex.dto.response.pokeapi.model.FlavorTextModel;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.HabitatModel;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;

import java.util.List;

@Serdeable(naming = SnakeCaseStrategy.class)
public record PokeApiResponse(
        String name,
        HabitatModel habitat,
        Boolean isLegendary,
        List<FlavorTextModel> flavorTextEntries
) { }
