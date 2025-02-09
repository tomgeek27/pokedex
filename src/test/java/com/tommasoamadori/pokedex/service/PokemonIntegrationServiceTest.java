package com.tommasoamadori.pokedex.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import com.tommasoamadori.pokedex.exception.NoValidFlavorTextException;
import com.tommasoamadori.pokedex.exception.PokemonNotFoundException;
import com.tommasoamadori.pokedex.exception.UnexpectedResponseBodyException;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
@Property(name = "micronaut.http.services.funtranslations.url", value = "http://localhost:8888")
public class PokemonIntegrationServiceTest {

    @Inject
    PokemonBaseService pokemonService;

    private static final String POKEMON_SPECIES_PATH = "/api/v2/pokemon-species/";
    private static final String TRANSLATE_YODA_PATH = "/translate/yoda";
    private static final String TRANSLATE_SHAKESPEARE_PATH = "/translate/shakespeare";

    @Test
    @DisplayName("getPokemonInfo should return valid PokemonInfo when PokeApiClient response is valid")
    void getPokemonInfoShouldReturnPokemonInfo() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(responseBody)));

        PokemonInfoResponse pokemonInfo = pokemonService.getPokemonInfo(pokemonName);

        verify(getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName)));

        assertAll(
                () -> assertThat(pokemonInfo.getIsLegendary()).isTrue(),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo("It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.")
        );
    }

    @Test
    @DisplayName("getPokemonInfo should throws PokemonNotFoundException when PokeApiClient response is NOT_FOUND")
    void getPokemonInfoShouldThrowPokemonNotFoundExceptionWhenGet404() {
        final String pokemonName = "mewtwo";

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(notFound()));

        assertThrows(PokemonNotFoundException.class, () -> pokemonService.getPokemonInfo(pokemonName));

        verify(getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName)));
    }

    @Test
    @DisplayName("getPokemonInfo should throws UnexpectedResponseBodyException when PokeApiClient response has empty body")
    void getPokemonInfoShouldThrowUnexpectedResponseBodyExceptionWhenGet2xxWithNullBody() {
        final String pokemonName = "mewtwo";

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(ok(null)));

        assertThrows(UnexpectedResponseBodyException.class, () -> pokemonService.getPokemonInfo(pokemonName));

        verify(getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName)));
    }

    @Test
    @DisplayName("getPokemonInfo should throws NoValidFlavorTextException when PokeApiClient response has no valid translations")
    void getPokemonInfoShouldThrowNoValidFlavorTextExceptionWhenGetNoValidEngDescription() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo_without_eng_description.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(responseBody)));

        assertThrows(NoValidFlavorTextException.class, () -> pokemonService.getPokemonInfo(pokemonName));

        verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName)));
    }

    @CsvSource({
            "not_legendary_cave_pokemon.json,cave,false",
            "mewtwo.json,rare,true",
            "legendary_cave_pokemon.json,cave,true"
    })
    @ParameterizedTest(name = "getTranslatedPokemonInfo should return PokemonInfo with yoda translation when legendary is {2} and habitat is {1}")
    void getTranslatedPokemonInfoShouldReturnPokemonInfoWithYodaTranslation(String filename, String habitat, boolean isLegendary) throws IOException {
        final String pokemonName = "mewtwo";

        final String pokemonInfoResponseBody = Files.readString(Paths.get("src/test/resources/%s".formatted(filename)));
        final String yodaTranslationResponseBody = Files.readString(Paths.get("src/test/resources/yoda.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(pokemonInfoResponseBody)));

        stubFor(
                post(urlPathEqualTo(TRANSLATE_YODA_PATH))
                        .withRequestBody(matching("text=.*"))
                        .willReturn(okJson(yodaTranslationResponseBody)));

        PokemonInfoResponse pokemonInfo = pokemonService.getTranslatedPokemonInfo(pokemonName);

        assertAll(
                () -> verify(getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))),
                () -> verify(postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH)).withRequestBody(matching("text=.*"))),
                () -> assertThat(pokemonInfo.getIsLegendary()).isEqualTo(isLegendary),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo(habitat),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo("Created by a scientist after years of horrific gene splicing and dna engineering experiments,  it was.")
        );
    }

    @Test
    @DisplayName("getTranslatedPokemonInfo should return PokemonInfo with shakespeare translation when legendary is false and habitat is not cave")
    void getTranslatedPokemonInfoShouldReturnPokemonInfoWithShakespeareTranslation() throws IOException {
        final String pokemonName = "mewtwo";

        final String pokemonInfoResponseBody = Files.readString(Paths.get("src/test/resources/not_legendary_rare_pokemon.json"));
        final String shakespeareTranslationResponseBody = Files.readString(Paths.get("src/test/resources/shakespeare.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(pokemonInfoResponseBody)));

        stubFor(
                post(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH))
                        .withRequestBody(matching("text=.*"))
                        .willReturn(okJson(shakespeareTranslationResponseBody)));

        PokemonInfoResponse pokemonInfo = pokemonService.getTranslatedPokemonInfo(pokemonName);

        assertAll(
                () -> verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))),
                () -> verify(exactly(1), postRequestedFor(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH)).withRequestBody(matching("text=.*"))),
                () -> verify(exactly(0), postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH))),
                () -> assertThat(pokemonInfo.getIsLegendary()).isFalse(),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo("'t wast did create by a scientist after years of horrific gene splicing and dna engineering experiments.")
        );
    }

    @Test
    @DisplayName("getTranslatedPokemonInfo should return PokemonInfo with same description when problem occurs during translation")
    void getTranslatedPokemonInfoShouldReturnPokemonInfoWithSameDescriptionWhenCannotTranslate() throws IOException {
        final String pokemonName = "mewtwo";

        final String pokemonInfoResponseBody = Files.readString(Paths.get("src/test/resources/not_legendary_rare_pokemon.json"));

        stubFor(get(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))
                .willReturn(okJson(pokemonInfoResponseBody)));

        stubFor(post(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH))
                        .withRequestBody(matching("text=.*"))
                        .willReturn(serverError()));

        PokemonInfoResponse pokemonInfo = pokemonService.getTranslatedPokemonInfo(pokemonName);

        assertAll(
                () -> verify(exactly(1), getRequestedFor(urlEqualTo(POKEMON_SPECIES_PATH + pokemonName))),
                () -> verify(exactly(1), postRequestedFor(urlPathEqualTo(TRANSLATE_SHAKESPEARE_PATH)).withRequestBody(matching("text=.*"))),
                () -> verify(exactly(0), postRequestedFor(urlPathEqualTo(TRANSLATE_YODA_PATH))),
                () -> assertThat(pokemonInfo.getIsLegendary()).isFalse(),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokemonName),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo("rare"),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo("It was created by a scientist after years of horrific gene splicing and DNA engineering experiments.")
        );
    }
}
