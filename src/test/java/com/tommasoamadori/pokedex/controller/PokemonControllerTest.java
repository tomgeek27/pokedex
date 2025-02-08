package com.tommasoamadori.pokedex.controller;

import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import com.tommasoamadori.pokedex.service.PokemonService;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MicronautTest
public class PokemonControllerTest {

    @MockBean(PokemonService.class)
    PokemonService pokemonService() {
        return mock(PokemonService.class);
    }

    @Inject
    private PokemonService pokemonService;

    @Inject
    @Client("/pokemon")
    private HttpClient client;

    @Test
    void getPokemonInfo() {
        final String pokemonName = Instancio.of(String.class).withSeed(1).create();
        PokemonInfoResponse pokemonInfoResponse = Instancio.of(PokemonInfoResponse.class).withSeed(1).create();

        when(pokemonService.getPokemonInfo(eq(pokemonName))).thenReturn(pokemonInfoResponse);

        PokemonInfoResponse response = client.toBlocking().retrieve(pokemonName, PokemonInfoResponse.class);

        assertAll(
                () -> verify(pokemonService, times(1)).getPokemonInfo(eq(pokemonName)),
                () -> assertThat(response).isEqualTo(pokemonInfoResponse)
        );
    }

}
