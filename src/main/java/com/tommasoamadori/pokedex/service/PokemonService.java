package com.tommasoamadori.pokedex.service;

import com.tommasoamadori.pokedex.client.api.pokeapi.PokeApiClient;
import com.tommasoamadori.pokedex.constant.Language;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokemonInfoResponse;
import com.tommasoamadori.pokedex.exception.NoValidFlavorTextException;
import com.tommasoamadori.pokedex.exception.UnexpectedResponseBodyException;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@Primary
@RequiredArgsConstructor
public class PokemonService implements PokemonBaseService {

    private final PokeApiClient pokeApiClient;

    @Override
    public PokemonInfoResponse getPokemonInfo(String name) {
        HttpResponse<PokeApiResponse> pokemonInfoResponse = pokeApiClient.getPokemonInfo(name);

        PokeApiResponse pokemonInfo = pokemonInfoResponse.getBody().orElseThrow(() -> new UnexpectedResponseBodyException(PokeApiClient.class.getSimpleName()));

        String pokemonDescription = pokemonInfo.flavorTextEntries().stream()
                .filter(flavorTextModel -> flavorTextModel.language().name().equals(Language.EN.getCode()))
                .findAny()
                .orElseThrow(NoValidFlavorTextException::new)
                .flavorText();
        String pokemonName = pokemonInfo.name();
        String pokemonHabitatName = pokemonInfo.habitat().name();
        Boolean isLegendaryPokemon = pokemonInfo.isLegendary();

        return PokemonInfoResponse
                .builder()
                .name(pokemonName)
                .habitat(pokemonHabitatName)
                .description(pokemonDescription)
                .isLegendary(isLegendaryPokemon)
                .build();
    }
}
