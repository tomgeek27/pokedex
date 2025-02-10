package com.tommasoamadori.pokedex.controller;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest
@WireMockTest(httpPort = 8888)
@Property(name = "micronaut.http.services.pokeapi.url", value = "http://localhost:8888")
@Property(name = "micronaut.http.services.funtranslations.url", value = "http://localhost:8888")
public class PokemonControllerIntegrationTest {

    @Inject
    @Client("/pokemon")
    private HttpClient client;

    private static final String POKEMON_SPECIES_PATH = "/api/v2/pokemon-species/";
    private static final String TRANSLATE_YODA_PATH = "/translate/yoda";
    private static final String TRANSLATE_SHAKESPEARE_PATH = "/translate/shakespeare";

    @Test
    @DisplayName("GET /pokemon/mewtwo should return 200 with a fulfilled response body")
    void getPokemonInfoTest() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(responseBody)));

        HttpResponse<PokemonInfoResponse> pokemonInfoResponse = client.toBlocking().exchange(pokemonName, PokemonInfoResponse.class);

        assertAll(
                () -> assertThat(pokemonInfoResponse.code()).isEqualTo(HttpStatus.OK.getCode()),
                () -> assertThat(pokemonInfoResponse.body()).isNotNull(),
                () -> verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName)))
        );

        PokemonInfoResponse pokemonInfo = pokemonInfoResponse.body();

        assertAll(
                () -> assertThat(pokemonInfo.getIsLegendary()).isTrue(),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo("It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.")
        );
    }

    @MethodSource("provideHttpError")
    @ParameterizedTest(name = "GET /pokemon/some-pokemon in case {0} should return {2}")
    void getPokemonInfoWhenGetErrorFormPokeapiShouldReturnErrors(String errorCaseDescription, ResponseDefinitionBuilder response, int code) {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(response));

        HttpClientResponseException httpClientResponseException = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve(pokemonName));

        assertAll(
                () -> assertThat(httpClientResponseException.code()).isEqualTo(code),
                () -> verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName)))
        );
    }

    @Test
    @DisplayName("GET /pokemon/translated/mewtwo should return 200 with a fulfilled response body")
    void getTranslatedPokemonInfoTestWithYoda() throws IOException {
        final String pokemonName = "mewtwo";

        final String pokemonInfoResponseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));
        final String yodaTranslationResponseBody = Files.readString(Paths.get("src/test/resources/yoda.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(pokemonInfoResponseBody)));

        stubFor(
                post(urlPathEqualTo(TRANSLATE_YODA_PATH))
                        .withRequestBody(matching("text=.*"))
                        .willReturn(okJson(yodaTranslationResponseBody)));

        HttpResponse<PokemonInfoResponse> pokemonInfoResponse = client.toBlocking().exchange("translated/" + pokemonName, PokemonInfoResponse.class);

        assertAll(
                () -> assertThat(pokemonInfoResponse.code()).isEqualTo(HttpStatus.OK.getCode()),
                () -> assertThat(pokemonInfoResponse.body()).isNotNull(),
                () -> verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))),
                () -> verify(exactly(1), postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH))),
                () -> verify(exactly(0), postRequestedFor(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH)))
        );

        PokemonInfoResponse pokemonInfo = pokemonInfoResponse.body();

        assertAll(
                () -> assertThat(pokemonInfo.getIsLegendary()).isTrue(),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo("Created by a scientist after years of horrific gene splicing and dna engineering experiments,  it was.")
        );
    }

    @Test
    @DisplayName("GET /pokemon/translated/not_legendary_rare_pokemon should return 200 with a fulfilled response body")
    void getTranslatedPokemonInfoTestWithShakespeare() throws IOException {
        final String pokemonName = "mewtwo";

        final String pokemonInfoResponseBody = Files.readString(Paths.get("src/test/resources/not_legendary_rare_pokemon.json"));
        final String yodaTranslationResponseBody = Files.readString(Paths.get("src/test/resources/shakespeare.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(pokemonInfoResponseBody)));

        stubFor(
                post(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH))
                        .withRequestBody(matching("text=.*"))
                        .willReturn(okJson(yodaTranslationResponseBody)));

        HttpResponse<PokemonInfoResponse> pokemonInfoResponse = client.toBlocking().exchange("translated/" + pokemonName, PokemonInfoResponse.class);

        assertAll(
                () -> assertThat(pokemonInfoResponse.code()).isEqualTo(HttpStatus.OK.getCode()),
                () -> assertThat(pokemonInfoResponse.body()).isNotNull(),
                () -> verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))),
                () -> verify(exactly(0), postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH))),
                () -> verify(exactly(1), postRequestedFor(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH)))
        );

        PokemonInfoResponse pokemonInfo = pokemonInfoResponse.body();

        assertAll(
                () -> assertThat(pokemonInfo.getIsLegendary()).isFalse(),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo("'t wast did create by a scientist after years of horrific gene splicing and dna engineering experiments.")
        );
    }

    @MethodSource("provideHttpError")
    @ParameterizedTest(name = "GET /pokemon/translated/some-pokemon in case {0} should return {2}")
    void getTranslatedPokemonInfoWhenGetErrorFormPokeapiShouldReturnErrors(String errorCaseDescription, ResponseDefinitionBuilder response, int code) {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(response));

        HttpClientResponseException httpClientResponseException = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve(pokemonName));

        assertAll(
            () -> verify(exactly(1), getRequestedFor(urlPathEqualTo(POKEMON_SPECIES_PATH + pokemonName))),
            () -> verify(exactly(0), postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH))),
            () -> verify(exactly(0), postRequestedFor(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH))),
            () -> assertThat(httpClientResponseException.code()).isEqualTo(code)
        );
    }

    @Test
    @DisplayName("GET /pokemon/translated/some-pokemon with fun translations error should maintain same description")
    void getTranslatedPokemonInfoWhenGetErrorFromFunTranslationsShouldMaintainSameDescription() throws IOException {
        final String pokemonName = "mewtwo";
        final String pokemonInfoResponseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));
        final String flavorText = "It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.";

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(pokemonInfoResponseBody)));

        stubFor(
                post(urlPathEqualTo(TRANSLATE_YODA_PATH))
                        .withRequestBody(matching("text=.*"))
                        .willReturn(serverError()));

        HttpResponse<PokemonInfoResponse> pokemonInfoResponse = client.toBlocking().exchange("translated/" + pokemonName, PokemonInfoResponse.class);

        assertAll(
                () -> assertThat(pokemonInfoResponse.code()).isEqualTo(HttpStatus.OK.getCode()),
                () -> assertThat(pokemonInfoResponse.body()).isNotNull(),
                () -> verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))),
                () -> verify(exactly(1), postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH))),
                () -> verify(exactly(0), postRequestedFor(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH)))
        );

        PokemonInfoResponse pokemonInfo = pokemonInfoResponse.body();

        assertAll(
                () -> assertThat(pokemonInfo.getIsLegendary()).isTrue(),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo(flavorText)
        );

    }

    private static Stream<Arguments> provideHttpError() {
        return Stream.of(
                Arguments.of("not-existent", notFound(), HttpStatus.NOT_FOUND.getCode()),
                Arguments.of("empty-body-response", ok(), HttpStatus.INTERNAL_SERVER_ERROR.getCode()),
                Arguments.of("generic-error", serverError(), HttpStatus.INTERNAL_SERVER_ERROR.getCode())
        );
    }
}
