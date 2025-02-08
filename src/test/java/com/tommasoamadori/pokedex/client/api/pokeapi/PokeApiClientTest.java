package com.tommasoamadori.pokedex.client.api.pokeapi;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.HabitatModel;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@WireMockTest(httpPort = 8888)
@Property(name = "micronaut.http.services.pokeapi.url", value = "http://localhost:8888")
class PokeApiClientTest {

    public static final String POKEMON_SPECIES_PATH = "/api/v2/pokemon-species/";
    @Inject
    private PokeApiClient pokeApiClient;

    @Test
    @DisplayName("When calling getPokemonInfo with 'mewtwo', should return a PokeApiResponse correctly fulfilled")
    void getPokemonInfoTest() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(responseBody)));

        final HttpResponse<PokeApiResponse> getPokemonInfoResponse = pokeApiClient.getPokemonInfo(pokemonName);

        final PokeApiResponse mewtwo = getPokemonInfoResponse.body();

        verify(getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName)));

        assertAll(
                () -> assertThat(mewtwo.isLegendary()).isTrue(),
                () -> assertThat(mewtwo.name()).isEqualTo(pokemonName),
                () -> assertThat(mewtwo.habitat()).isEqualTo(new HabitatModel("rare")),
                () -> assertThat(mewtwo.flavorTextEntries()).isNotEmpty(),
                () -> assertThat(mewtwo.flavorTextEntries().get(0).flavorText()).contains("It was created by\na scientist after\nyears of horrific\fgene splicing"),
                () -> assertThat(mewtwo.flavorTextEntries().get(0).language().name()).isEqualTo("en")
        );
    }

    @Test
    @DisplayName("When calling getPokemonInfo with blank string as name, should throws")
    void getPokemonInfoWithBlankStringTest() {
        final String blankString = " ";

        assertThrows(
                ConstraintViolationException.class,
                () -> pokeApiClient.getPokemonInfo(blankString)
        );
    }

    @MethodSource("provideErrorHttp")
    @ParameterizedTest(name = "When getPokemonInfo return an error status code ({1}), should throws HttpClientResponseException")
    void getPokemonInfoWithErrorStatusShouldThrowsHttpClientResponseException(ResponseDefinitionBuilder response, int code) {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(response));

        assertThrows(
                HttpClientResponseException.class,
                () -> pokeApiClient.getPokemonInfo(pokemonName)
        );
    }

    @Test
    @DisplayName("When getPokemonInfo return a 404, should NOT throws HttpClientResponseException")
    void getPokemonInfoWith404ShouldNotThrowsHttpClientResponseException() {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(notFound()));

        assertDoesNotThrow(() -> pokeApiClient.getPokemonInfo(pokemonName));
    }

    private static Stream<Arguments> provideErrorHttp() {
        return Stream.of(
            Arguments.of(badRequest(), HttpResponse.badRequest().code()),
            Arguments.of(serverError(), HttpResponse.serverError().code())
        );
    }
}