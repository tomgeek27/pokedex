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
public class PokemonControllerIntegrationTest {

    @Inject
    @Client("/pokemon")
    private HttpClient client;

    @Test
    @DisplayName("GET /pokemon/mewtwo should return 200 with a fulfilled response body")
    void getPokemonInfoTest() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));

        stubFor(get(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))
                .willReturn(okJson(responseBody)));

        HttpResponse<PokemonInfoResponse> pokemonInfoResponse = client.toBlocking().exchange(pokemonName, PokemonInfoResponse.class);

        assertAll(
                () -> verify(getRequestedFor(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))),
                () -> assertThat(pokemonInfoResponse.code()).isEqualTo(HttpStatus.OK.getCode()),
                () -> assertThat(pokemonInfoResponse.body()).isNotNull(),
                () -> assertThat(pokemonInfoResponse.body().getIsLegendary()).isTrue(),
                () -> assertThat(pokemonInfoResponse.body().getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfoResponse.body().getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfoResponse.body().getDescription()).isEqualTo("It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.")
        );
    }

    @MethodSource("provideHttpError")
    @ParameterizedTest(name = "GET /pokemon/some-pokemon with {0} should return {2}")
    void getPokemonInfoWhenGetErrorFormPokeapiShouldReturnErrors(String errorCaseDescription, ResponseDefinitionBuilder response, int code) {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();

        stubFor(get(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))
                .willReturn(response));

        HttpClientResponseException httpClientResponseException = assertThrows(HttpClientResponseException.class, () -> client.toBlocking().retrieve(pokemonName));

        assertThat(httpClientResponseException.code()).isEqualTo(code);
    }

    private static Stream<Arguments> provideHttpError() {
        return Stream.of(
                Arguments.of("not-existent", notFound(), HttpStatus.NOT_FOUND.getCode()),
                Arguments.of("empty-body-response", ok(), HttpStatus.INTERNAL_SERVER_ERROR.getCode()),
                Arguments.of("generic-error", serverError(), HttpStatus.INTERNAL_SERVER_ERROR.getCode())
        );
    }
}
