package com.tommasoamadori.pokedex.client.api.funtranslations;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tommasoamadori.pokedex.dto.response.funtranslations.FunTranslationsResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
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
import java.util.Map;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest
@WireMockTest(httpPort = 8888)
@Property(name = "micronaut.http.services.funtranslations.url", value = "http://localhost:8888")
class FunTranslationsClientTest {

    private static final String TRANSLATE_YODA_PATH = "/translate/yoda";
    private static final String TRANSLATE_SHAKESPEARE_PATH = "/translate/shakespeare";

    @Inject
    private FunTranslationsClient funTranslationsClient;

    @Test
    @DisplayName("When calling translateYoda with some text, should return a FunTranslationsResponse correctly fulfilled")
    void getYodaTranslationTest() throws IOException {
        final String text = Instancio.of(String.class).withSeed(1).create();

        final String responseBody = Files.readString(Paths.get("src/test/resources/yoda.json"));
        stubFor(
                post(urlPathEqualTo(TRANSLATE_YODA_PATH))
                        .withRequestBody(containing("text=" + text))
                        .willReturn(okJson(responseBody)));

        final HttpResponse<FunTranslationsResponse> funTranslationsResponse = funTranslationsClient.translateYoda(Map.of("text", text));

        final FunTranslationsResponse yodaTranslation = funTranslationsResponse.body();

        assertAll(
                () -> verify(postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH)).withRequestBody(containing("text=" + text))),
                () -> assertThat(yodaTranslation).isNotNull(),
                () -> assertThat(yodaTranslation.contents().translated()).isEqualTo("Created by a scientist after years of horrific genesplicing and dna engineering experiments,  it was.")
        );
    }

    @Test
    @DisplayName("When calling translateShakespeare with some text, should return a FunTranslationsResponse correctly fulfilled")
    void getShakespeareTranslationTest() throws IOException {
        final String text = Instancio.of(String.class).withSeed(1).create();

        final String responseBody = Files.readString(Paths.get("src/test/resources/shakespeare.json"));
        stubFor(
                post(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH))
                        .withRequestBody(containing("text=" + text))
                        .willReturn(okJson(responseBody)));

        final HttpResponse<FunTranslationsResponse> funTranslationsResponse = funTranslationsClient.translateShakespeare(Map.of("text", text));

        final FunTranslationsResponse yodaTranslation = funTranslationsResponse.body();

        assertAll(
                () -> verify(postRequestedFor(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH))
                        .withRequestBody(containing("text=" + text))
                ),
                () -> assertThat(yodaTranslation).isNotNull(),
                () -> assertThat(yodaTranslation.contents().translated()).isEqualTo("'t wast did create by a scientist after years of horrific genesplicing and dna engineering experiments.")
        );
    }

    @MethodSource("provideErrorHttp")
    @ParameterizedTest(name = "When translateYoda return an error status code ({1}), should throws HttpClientResponseException")
    void getYodaTranslationWithErrorStatusShouldThrowsHttpClientResponseException(ResponseDefinitionBuilder response, int code) {
        final String text = Instancio.of(String.class).withSeed(1).create();

        stubFor(
                post(urlPathEqualTo(TRANSLATE_YODA_PATH))
                        .withRequestBody(containing("text=" + text))
                        .willReturn(response));

        assertThrows(
                HttpClientResponseException.class,
                () -> funTranslationsClient.translateYoda(Map.of("text", text))
        );
    }

    @MethodSource("provideErrorHttp")
    @ParameterizedTest(name = "When translateShakespeare return an error status code ({1}), should throws HttpClientResponseException")
    void getShakespeareTranslationWithErrorStatusShouldThrowsHttpClientResponseException(ResponseDefinitionBuilder response, int code) {
        final String text = Instancio.of(String.class).withSeed(1).create();

        stubFor(
                post(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH))
                        .withRequestBody(
                                containing("text=" + text)
                        )
                        .willReturn(response));

        assertThrows(
                HttpClientResponseException.class,
                () -> funTranslationsClient.translateShakespeare(Map.of("text", text))
        );
    }

    private static Stream<Arguments> provideErrorHttp() {
        return Stream.of(
                Arguments.of(badRequest(), HttpResponse.badRequest().code()),
                Arguments.of(serverError(), HttpResponse.serverError().code())
        );
    }

}