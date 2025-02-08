package com.tommasoamadori.pokedex.service;

import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import jakarta.inject.Singleton;

@Singleton
public interface PokemonBaseService {

    PokemonInfoResponse getPokemonInfo(String name);

}
