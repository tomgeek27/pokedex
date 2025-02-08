package com.tommasoamadori.pokedex.client.api.pokeapi;

import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.client.annotation.Client;
import jakarta.validation.constraints.NotBlank;

@Client(id = "pokeapi")
public interface PokeApiClient {

    @Get("api/v2/pokemon-species/{name}")
    HttpResponse<PokeApiResponse> getPokemonInfo(@PathVariable @NotBlank String name);

}
