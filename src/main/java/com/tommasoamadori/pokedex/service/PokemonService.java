package com.tommasoamadori.pokedex.service;

import com.tommasoamadori.pokedex.client.api.pokeapi.PokeApiClient;
import com.tommasoamadori.pokedex.constant.Language;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokemonInfoResponse;
import com.tommasoamadori.pokedex.exception.NoValidFlavorTextException;
import com.tommasoamadori.pokedex.exception.PokemonNotFoundException;
import com.tommasoamadori.pokedex.exception.UnexpectedResponseBodyException;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for retrieving Pokémon information.
 */
@Slf4j
@Singleton
@Primary
@RequiredArgsConstructor
public class PokemonService implements PokemonBaseService {

    private final PokeApiClient pokeApiClient;

    /**
     * Retrieves information about a Pokémon, including its name,
     * habitat, description, and legendary status.
     *
     * @param name The name of the Pokémon to retrieve.
     * @return A {@link PokemonInfoResponse} containing the Pokémon details.
     * @throws NoValidFlavorTextException if no English flavor text is found.
     * @throws UnexpectedResponseBodyException if the API response is invalid.
     * @throws PokemonNotFoundException if the Pokemon does not exists.
     */
    @Override
    public PokemonInfoResponse getPokemonInfo(String name) {
        HttpResponse<PokeApiResponse> pokemonInfoResponse = pokeApiClient.getPokemonInfo(name);

        PokeApiResponse pokemonInfo = pokemonInfoResponse.getBody().orElseThrow(() -> {
            if(pokemonInfoResponse.code() == HttpStatus.NOT_FOUND.getCode()) {
                log.error("Pokémon {} not found", name);
                return new PokemonNotFoundException(name);
            }

            log.error("Empty response body");
            return new UnexpectedResponseBodyException(PokeApiClient.class.getSimpleName());
        });

        String pokemonDescription = pokemonInfo.flavorTextEntries().stream()
                .filter(flavorTextModel -> flavorTextModel.language().name().equals(Language.EN.getCode()))
                .findAny()
                .orElseThrow(() -> {
                    log.error("No valid flavor text found");
                    return new NoValidFlavorTextException();
                })
                .flavorText();
        String pokemonName = pokemonInfo.name();
        String pokemonHabitatName = pokemonInfo.habitat().name();
        Boolean isLegendaryPokemon = pokemonInfo.isLegendary();

        log.info("Retrieved {} information", name);

        return PokemonInfoResponse
                .builder()
                .name(pokemonName)
                .habitat(pokemonHabitatName)
                .description(pokemonDescription)
                .isLegendary(isLegendaryPokemon)
                .build();
    }
}
