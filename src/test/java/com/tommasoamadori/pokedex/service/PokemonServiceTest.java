package com.tommasoamadori.pokedex.service;

import com.tommasoamadori.pokedex.client.api.pokeapi.PokeApiClient;
import com.tommasoamadori.pokedex.constant.Language;
import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.FlavorLanguageModel;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.FlavorTextModel;
import com.tommasoamadori.pokedex.exception.NoValidFlavorTextException;
import com.tommasoamadori.pokedex.exception.UnexpectedResponseBodyException;
import io.micronaut.http.HttpResponse;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@MicronautTest
public class PokemonServiceTest {

    @Inject
    private PokemonService pokemonService;

    @MockBean(PokeApiClient.class)
    PokeApiClient pokeApiClient() {
        return mock(PokeApiClient.class);
    }

    @Inject
    PokeApiClient pokeApiClient;

    @Test
    @DisplayName("getPokemonInfo should call pokeApiClient and build a correct PokemonInfoResponse")
    void getPokemonInfoShouldReturnPokemonInfoFromPokeApi() {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        final FlavorLanguageModel en = Instancio.of(FlavorLanguageModel.class).withSeed(1)
                .set(field(FlavorLanguageModel::name), Language.EN.getCode())
                .create();

        final FlavorLanguageModel it = Instancio.of(FlavorLanguageModel.class).withSeed(2)
                .set(field(FlavorLanguageModel::name), "it")
                .create();

        final FlavorTextModel enTextModel = Instancio.of(FlavorTextModel.class).set(field(FlavorTextModel::language), en).create();
        final FlavorTextModel itTextModel = Instancio.of(FlavorTextModel.class).set(field(FlavorTextModel::language), it).create();

        final PokeApiResponse pokeApiResponse = Instancio
                .of(PokeApiResponse.class)
                .withSeed(1)
                .set(field(PokeApiResponse::name), pokemonName)
                .set(field(PokeApiResponse::flavorTextEntries), List.of(enTextModel, itTextModel))
                .create();

        when(pokeApiClient.getPokemonInfo(pokemonName)).thenReturn(HttpResponse.ok(pokeApiResponse));

        PokemonInfoResponse pokemonInfo = pokemonService.getPokemonInfo(pokemonName);

        assertAll(
                () -> verify(pokeApiClient, times(1)).getPokemonInfo(eq(pokemonName)),
                () -> assertThat(pokemonInfo.description()).isEqualTo(enTextModel.flavorText()),
                () -> assertThat(pokemonInfo.habitat()).isEqualTo(pokeApiResponse.habitat().name()),
                () -> assertThat(pokemonInfo.isLegendary()).isEqualTo(pokeApiResponse.isLegendary()),
                () -> assertThat(pokemonInfo.name()).isEqualTo(pokeApiResponse.name())
        );
    }

    @Test
    @DisplayName("getPokemonInfo should throw NoValidFlavorTextException when en language is not present")
    void getPokemonInfoShouldThrowWithNotValidLanguage() {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        final FlavorLanguageModel it = Instancio.of(FlavorLanguageModel.class).withSeed(2)
                .set(field(FlavorLanguageModel::name), "it")
                .create();

        final FlavorTextModel itTextModel = Instancio.of(FlavorTextModel.class).set(field(FlavorTextModel::language), it).create();

        final PokeApiResponse pokeApiResponse = Instancio
                .of(PokeApiResponse.class)
                .withSeed(1)
                .set(field(PokeApiResponse::name), pokemonName)
                .set(field(PokeApiResponse::flavorTextEntries), List.of(itTextModel))
                .create();

        when(pokeApiClient.getPokemonInfo(pokemonName)).thenReturn(HttpResponse.ok(pokeApiResponse));

        assertThrows(NoValidFlavorTextException.class, () -> pokemonService.getPokemonInfo(pokemonName));
    }

    @Test
    @DisplayName("getPokemonInfo should throw if no valid body is returned from PokeApiClient")
    void getPokemonInfoShouldThrowWithNotValidBodyResponse() {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        when(pokeApiClient.getPokemonInfo(pokemonName)).thenReturn(HttpResponse.ok(null));

        assertThrows(UnexpectedResponseBodyException.class, () -> pokemonService.getPokemonInfo(pokemonName));
    }

}
