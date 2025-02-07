package com.tommasoamadori.pokedex.client.api.pokeapi;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import com.tommasoamadori.pokedex.dto.response.pokeapi.model.HabitatModel;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
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

@MicronautTest
@WireMockTest(httpPort = 8888)
@Property(name = "micronaut.http.services.pokeapi.url", value = "http://localhost:8888")
class PokeApiClientTest {

    @Inject
    private PokeApiClient pokeApiClient;

    @Test
    @DisplayName("When calling getPokemonInfo with 'mewtwo', should return a PokeApiResponse correctly fulfilled")
    void getPokemonInfoTest() throws IOException {
        final String pokemonName = "mewtwo";

        final String responseBody = Files.readString(Paths.get("src/test/resources/mewtwo.json"));

        stubFor(get(urlEqualTo("/api/v2/pokemon-species/" + pokemonName))
                .willReturn(okJson(responseBody)));

        final HttpResponse<PokeApiResponse> getPokemonInfoResponse = pokeApiClient.getPokemonInfo(pokemonName);

        final PokeApiResponse mewtwo = getPokemonInfoResponse.body();

        verify(getRequestedFor(urlEqualTo("/api/v2/pokemon-species/" + pokemonName)));

        assertAll(
                () -> assertThat(mewtwo.isLegendary()).isTrue(),
                () -> assertThat(mewtwo.name()).isEqualTo(pokemonName),
                () -> assertThat(mewtwo.habitat()).isEqualTo(new HabitatModel("rare")),
                () -> assertThat(mewtwo.flavorTextEntries()).isNotEmpty(),
                () -> assertThat(mewtwo.flavorTextEntries().get(0).flavorText()).contains("It was created by\na scientist after\nyears of horrific\fgene splicing"),
                () -> assertThat(mewtwo.flavorTextEntries().get(0).language().name()).isEqualTo("en")
        );
    }
}