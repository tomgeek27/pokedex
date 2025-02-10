package com.tommasoamadori.pokedex.controller;

import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import com.tommasoamadori.pokedex.exception.PokemonNotFoundException;
import com.tommasoamadori.pokedex.service.PokemonBaseService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing Pokémon information requests.
 */
@Slf4j
@OpenAPIDefinition
@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("pokemon")
@RequiredArgsConstructor
public class PokemonController {

    private final PokemonBaseService pokemonService;

    /**
     * Get information about a specific Pokémon by name.
     *
     * @param name The name of the Pokémon.
     * @return The Pokémon information.
     */
    @Operation(
        summary = "Get information about a specific Pokémon by name",
        description = "Fetches detailed information about a Pokémon based on its name"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Pokémon information", content = @Content(schema = @Schema(implementation = PokemonInfoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pokémon not found")
    @Get(value = "{name}", produces = MediaType.APPLICATION_JSON)
    public PokemonInfoResponse pokemon(
            @Parameter(description = "Pokémon name")
            @PathVariable @NotBlank String name) {
        log.info("Incoming info request for {}", name);
        PokemonInfoResponse pokemonInfo = pokemonService.getPokemonInfo(name);
        log.info("{} info: {}", name, pokemonInfo);

        return pokemonInfo;
    }

    /**
     * Get information about a specific Pokémon by name with fun translated description.
     *
     * @param name The name of the Pokémon.
     * @return The Pokémon information with fun translated description.
     */
    @Operation(
            summary = "Get information about a specific Pokémon by name with a fun description translation",
            description = "Fetches detailed information with a fun description translation about a Pokémon based on its name"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved Pokémon information with translated description", content = @Content(schema = @Schema(implementation = PokemonInfoResponse.class)))
    @ApiResponse(responseCode = "404", description = "Pokémon not found")
    @Get(value = "translated/{name}", produces = MediaType.APPLICATION_JSON)
    public PokemonInfoResponse translatedPokemon(
            @Parameter(description = "Pokémon name")
            @PathVariable @NotBlank String name) {
        log.info("Incoming translated info request for {}", name);
        PokemonInfoResponse translatedPokemonInfo = pokemonService.getTranslatedPokemonInfo(name);
        log.info("{} translated info: {}", name, translatedPokemonInfo);

        return translatedPokemonInfo;
    }

    @Error(exception = PokemonNotFoundException.class)
    public HttpResponse<String> handlePokemonNotFound(PokemonNotFoundException e) {
        return HttpResponse.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

}
