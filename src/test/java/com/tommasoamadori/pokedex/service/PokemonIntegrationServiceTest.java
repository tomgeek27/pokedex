package com.tommasoamadori.pokedex.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokemonInfoResponse;
import com.tommasoamadori.pokedex.exception.NoValidFlavorTextException;
import com.tommasoamadori.pokedex.exception.PokemonNotFoundException;
import com.tommasoamadori.pokedex.exception.UnexpectedResponseBodyException;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest
@WireMockTest(httpPort = 8888)
@Property(name = "micronaut.http.services.pokeapi.url", value = "http://localhost:8888")
public class PokemonIntegrationServiceTest {

    @Inject
    PokemonBaseService pokemonService;

    @Test
    @DisplayName("getPokemonInfo should return valid PokemonInfo when PokeApiClient response is valid")
    void getPokemonInfoShouldReturnPokemonInfo() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));

        stubFor(get(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))
                .willReturn(okJson(responseBody)));

        PokemonInfoResponse pokemonInfo = pokemonService.getPokemonInfo(pokemonName);

        verify(getRequestedFor(urlEqualTo("/api/v2/pokemon-species/" + pokemonName)));

        assertAll(
                () -> assertThat(pokemonInfo.isLegendary()).isTrue(),
                () -> assertThat(pokemonInfo.name()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.habitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.description()).contains("It was created by\na scientist after\nyears of horrific\fgene splicing")
        );
    }

    @Test
    @DisplayName("getPokemonInfo should throws PokemonNotFoundException when PokeApiClient response is NOT_FOUND")
    void getPokemonInfoShouldThrowPokemonNotFoundExceptionWhenGet404() {
        final String pokemonName = "mewtwo";

        stubFor(get(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))
                .willReturn(notFound()));

        assertThrows(PokemonNotFoundException.class, () -> pokemonService.getPokemonInfo(pokemonName));

        verify(getRequestedFor(urlEqualTo("/api/v2/pokemon-species/" + pokemonName)));
    }

    @Test
    @DisplayName("getPokemonInfo should throws UnexpectedResponseBodyException when PokeApiClient response has empty body")
    void getPokemonInfoShouldThrowUnexpectedResponseBodyExceptionWhenGet2xxWithNullBody() {
        final String pokemonName = "mewtwo";

        stubFor(get(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))
                .willReturn(ok(null)));

        assertThrows(UnexpectedResponseBodyException.class, () -> pokemonService.getPokemonInfo(pokemonName));

        verify(getRequestedFor(urlEqualTo("/api/v2/pokemon-species/" + pokemonName)));
    }

    @Test
    @DisplayName("getPokemonInfo should throws NoValidFlavorTextException when PokeApiClient response has no valid translations")
    void getPokemonInfoShouldThrowNoValidFlavorTextExceptionWhenGetNoValidEngDescription() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo_without_eng_description.json"));

        stubFor(get(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))
                .willReturn(okJson(responseBody)));

        assertThrows(NoValidFlavorTextException.class, () -> pokemonService.getPokemonInfo(pokemonName));

        verify(getRequestedFor(urlEqualTo("/api/v2/pokemon-species/" + pokemonName)));
    }

}
