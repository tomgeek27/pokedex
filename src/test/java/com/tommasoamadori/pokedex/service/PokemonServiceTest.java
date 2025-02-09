package com.tommasoamadori.pokedex.service;

import com.tommasoamadori.pokedex.client.api.funtranslations.FunTranslationsClient;
import com.tommasoamadori.pokedex.client.api.pokeapi.PokeApiClient;
import com.tommasoamadori.pokedex.constant.Language;
import com.tommasoamadori.pokedex.dto.request.funtranslations.TranslateRequest;
import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import com.tommasoamadori.pokedex.dto.response.funtranslations.FunTranslationsResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.FlavorLanguageModel;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.FlavorTextModel;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.HabitatModel;
import com.tommasoamadori.pokedex.exception.NoValidFlavorTextException;
import com.tommasoamadori.pokedex.exception.UnexpectedResponseBodyException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

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
    private PokeApiClient pokeApiClient() {
        return mock(PokeApiClient.class);
    }

    @Inject
    private PokeApiClient pokeApiClient;

    @MockBean(FunTranslationsClient.class)
    private FunTranslationsClient funTranslationsClient() {
        return mock(FunTranslationsClient.class);
    }

    @Inject
    private FunTranslationsClient funTranslationsClient;

    private static final String pokemonName = Instancio.of(String.class).withSeed(1).create();

    private static final FlavorLanguageModel en = Instancio.of(FlavorLanguageModel.class).withSeed(1)
            .set(field(FlavorLanguageModel::name), Language.EN.getCode())
            .create();

    private static final FlavorLanguageModel it = Instancio.of(FlavorLanguageModel.class).withSeed(2)
            .set(field(FlavorLanguageModel::name), "it")
            .create();

    private static final FlavorTextModel enTextModel = Instancio.of(FlavorTextModel.class).set(field(FlavorTextModel::language), en).create();
    private static final FlavorTextModel itTextModel = Instancio.of(FlavorTextModel.class).set(field(FlavorTextModel::language), it).create();

    private static final HabitatModel habitatCaveModel = Instancio.of(HabitatModel.class).withSeed(1)
            .set(field(HabitatModel::name), "cave")
            .create();

    private static final HabitatModel habitatRareModel = Instancio.of(HabitatModel.class).withSeed(1)
            .set(field(HabitatModel::name), "rare")
            .create();

    @Test
    @DisplayName("getPokemonInfo should call pokeApiClient and build a correct PokemonInfoResponse")
    void getPokemonInfoShouldReturnPokemonInfoFromPokeApi() {
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
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo(enTextModel.flavorText()),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo(pokeApiResponse.habitat().name()),
                () -> assertThat(pokemonInfo.getIsLegendary()).isEqualTo(pokeApiResponse.isLegendary()),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokeApiResponse.name())
        );
    }

    @Test
    @DisplayName("getPokemonInfo should throw NoValidFlavorTextException when en language is not present")
    void getPokemonInfoShouldThrowWithNotValidLanguage() {
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

    @MethodSource("providePokeApiResponseForYodaTranslation")
    @ParameterizedTest(name = "getTranslatedPokemonInfo should call pokeApiClient, funTranslationClient with yoda translation when is {1} pokémon")
    void getTranslatedPokemonInfoShouldReturnYodaTranslationWhenIsLegendaryPokemon(PokeApiResponse pokeApiResponse, String characteristic) {
        final FunTranslationsResponse funTranslationsResponse =
                Instancio.of(FunTranslationsResponse.class).withSeed(1).create();

        final TranslateRequest translateRequest = new TranslateRequest(enTextModel.flavorText());

        when(pokeApiClient.getPokemonInfo(pokemonName)).thenReturn(HttpResponse.ok(pokeApiResponse));
        when(funTranslationsClient.translateYoda(translateRequest)).thenReturn(HttpResponse.ok(funTranslationsResponse));

        PokemonInfoResponse pokemonInfo = pokemonService.getTranslatedPokemonInfo(pokemonName);

        assertAll(
                () -> verify(pokeApiClient, times(1)).getPokemonInfo(eq(pokemonName)),
                () -> verify(funTranslationsClient, times(1)).translateYoda(eq(translateRequest)),
                () -> verify(funTranslationsClient, times(0)).translateShakespeare(any()),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo(funTranslationsResponse.contents().translated()),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo(pokeApiResponse.habitat().name()),
                () -> assertThat(pokemonInfo.getIsLegendary()).isEqualTo(pokeApiResponse.isLegendary()),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(pokeApiResponse.name())
        );
    }

    @Test
    @DisplayName("getTranslatedPokemonInfo should call pokeApiClient, funTranslationClient with shakespeare translation when is not legendary pokémon")
    void getTranslatedPokemonInfoShouldReturnShakespeareTranslation() {
        final PokeApiResponse notLegendaryPokemon = Instancio
                .of(PokeApiResponse.class)
                .withSeed(1)
                .set(field(PokeApiResponse::name), pokemonName)
                .set(field(PokeApiResponse::isLegendary), false)
                .set(field(PokeApiResponse::habitat), habitatRareModel)
                .set(field(PokeApiResponse::flavorTextEntries), List.of(enTextModel, itTextModel))
                .create();

        final FunTranslationsResponse funTranslationsResponse =
                Instancio.of(FunTranslationsResponse.class).withSeed(1).create();

        final TranslateRequest translateRequest = new TranslateRequest(enTextModel.flavorText());

        when(pokeApiClient.getPokemonInfo(pokemonName)).thenReturn(HttpResponse.ok(notLegendaryPokemon));
        when(funTranslationsClient.translateShakespeare(translateRequest)).thenReturn(HttpResponse.ok(funTranslationsResponse));

        PokemonInfoResponse pokemonInfo = pokemonService.getTranslatedPokemonInfo(pokemonName);

        assertAll(
                () -> verify(pokeApiClient, times(1)).getPokemonInfo(eq(pokemonName)),
                () -> verify(funTranslationsClient, times(0)).translateYoda(any()),
                () -> verify(funTranslationsClient, times(1)).translateShakespeare(eq(translateRequest)),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo(funTranslationsResponse.contents().translated()),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo(notLegendaryPokemon.habitat().name()),
                () -> assertThat(pokemonInfo.getIsLegendary()).isEqualTo(notLegendaryPokemon.isLegendary()),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(notLegendaryPokemon.name())
        );
    }


    @Test
    @DisplayName("getTranslatedPokemonInfo should maintain the same description when some exception occurs during translations")
    void getTranslatedPokemonInfoShouldMaintainSameDescriptionWhenExceptionIsThrown() {
        final PokeApiResponse notLegendaryPokemon = Instancio
                .of(PokeApiResponse.class)
                .withSeed(1)
                .set(field(PokeApiResponse::name), pokemonName)
                .set(field(PokeApiResponse::isLegendary), false)
                .set(field(PokeApiResponse::habitat), habitatRareModel)
                .set(field(PokeApiResponse::flavorTextEntries), List.of(enTextModel, itTextModel))
                .create();

        final TranslateRequest translateRequest = new TranslateRequest(enTextModel.flavorText());

        when(pokeApiClient.getPokemonInfo(pokemonName)).thenReturn(HttpResponse.ok(notLegendaryPokemon));
        when(funTranslationsClient.translateShakespeare(translateRequest)).thenThrow(new HttpClientResponseException("A problem has occured", HttpResponse.serverError()));

        PokemonInfoResponse pokemonInfo = pokemonService.getTranslatedPokemonInfo(pokemonName);

        assertAll(
                () -> verify(pokeApiClient, times(1)).getPokemonInfo(eq(pokemonName)),
                () -> verify(funTranslationsClient, times(0)).translateYoda(any()),
                () -> verify(funTranslationsClient, times(1)).translateShakespeare(eq(translateRequest)),
                () -> assertThat(pokemonInfo.getDescription()).isEqualTo(enTextModel.flavorText()),
                () -> assertThat(pokemonInfo.getHabitat()).isEqualTo(notLegendaryPokemon.habitat().name()),
                () -> assertThat(pokemonInfo.getIsLegendary()).isEqualTo(notLegendaryPokemon.isLegendary()),
                () -> assertThat(pokemonInfo.getName()).isEqualTo(notLegendaryPokemon.name())
        );
    }

    private static Stream<Arguments> providePokeApiResponseForYodaTranslation() {
        final PokeApiResponse legendaryPokemon = Instancio
                .of(PokeApiResponse.class)
                .withSeed(1)
                .set(field(PokeApiResponse::name), pokemonName)
                .set(field(PokeApiResponse::isLegendary), true)
                .set(field(PokeApiResponse::flavorTextEntries), List.of(enTextModel, itTextModel))
                .create();

        final PokeApiResponse cavePokemon = Instancio
                .of(PokeApiResponse.class)
                .withSeed(1)
                .set(field(PokeApiResponse::name), pokemonName)
                .set(field(PokeApiResponse::habitat), habitatCaveModel)
                .set(field(PokeApiResponse::flavorTextEntries), List.of(enTextModel, itTextModel))
                .create();

        return Stream.of(
                Arguments.of(legendaryPokemon, "legendary"),
                Arguments.of(cavePokemon, "cave")
        );
    }

}
