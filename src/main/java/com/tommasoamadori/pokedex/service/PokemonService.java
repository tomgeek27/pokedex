package com.tommasoamadori.pokedex.service;

import com.tommasoamadori.pokedex.client.api.funtranslations.FunTranslationsClient;
import com.tommasoamadori.pokedex.client.api.pokeapi.PokeApiClient;
import com.tommasoamadori.pokedex.constant.Language;
import com.tommasoamadori.pokedex.dto.request.funtranslations.TranslateRequest;
import com.tommasoamadori.pokedex.dto.response.PokemonInfoResponse;
import com.tommasoamadori.pokedex.dto.response.funtranslations.FunTranslationsResponse;
import com.tommasoamadori.pokedex.dto.response.funtranslations.model.TranslationContentModel;
import com.tommasoamadori.pokedex.dto.response.pokeapi.PokeApiResponse;
import com.tommasoamadori.pokedex.exception.NoValidFlavorTextException;
import com.tommasoamadori.pokedex.exception.PokemonNotFoundException;
import com.tommasoamadori.pokedex.exception.UnexpectedResponseBodyException;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

/**
 * Service responsible for retrieving Pokémon information.
 */
@Slf4j
@Singleton
@Primary
@RequiredArgsConstructor
public class PokemonService implements PokemonBaseService {

    private final PokeApiClient pokeApiClient;
    private final FunTranslationsClient funTranslationsClient;

    /**
     * Retrieves information about a Pokémon, including its name,
     * habitat, description, and legendary status.
     *
     * @param name The name of the Pokémon to retrieve.
     * @return A {@link PokemonInfoResponse} containing the Pokémon details.
     * @throws NoValidFlavorTextException if no English flavor text is found.
     * @throws UnexpectedResponseBodyException if the API response is invalid.
     * @throws PokemonNotFoundException if the Pokemon does not exists.
     */
    @Override
    public PokemonInfoResponse getPokemonInfo(String name) {
        return retrievePokemonInfo(name);
    }

    /**
     * Retrieves information about a Pokémon and returns its description
     * translated into Yoda or Shakespeare style based on its characteristics.
     *
     * <p>
     * Translation rules:
     * <ol>
     *     <li>If the Pokémon's habitat is "cave" or it is legendary, apply the Yoda translation.</li>
     *     <li>Otherwise, apply the Shakespeare translation.</li>
     *     <li>If translation fails, the original description is returned.</li>
     * </ol>
     * </p>
     *
     * @param name The name of the Pokémon to retrieve.
     * @return A {@link PokemonInfoResponse} containing the Pokémon details with translated description.
     */
    @Override
    public PokemonInfoResponse getTranslatedPokemonInfo(String name) {
        PokemonInfoResponse pokemonInfoResponse = retrievePokemonInfo(name);

        Optional<String> oTranslation = tryTranslateDescription(pokemonInfoResponse);

        oTranslation.ifPresent(pokemonInfoResponse::setDescription);

        return pokemonInfoResponse;
    }

    private PokemonInfoResponse retrievePokemonInfo(String name) {
        HttpResponse<PokeApiResponse> pokemonInfoResponse = pokeApiClient.getPokemonInfo(name);

        PokeApiResponse pokemonInfo = pokemonInfoResponse.getBody().orElseThrow(() -> {
            if(pokemonInfoResponse.code() == HttpStatus.NOT_FOUND.getCode()) {
                log.error("Pokémon {} not found", name);
                return new PokemonNotFoundException(name);
            }

            log.error("Empty response body");
            return new UnexpectedResponseBodyException(PokeApiClient.class.getSimpleName());
        });

        String pokemonDescription = pokemonInfo.flavorTextEntries().stream()
                .filter(flavorTextModel -> flavorTextModel.language().name().equals(Language.EN.getCode()))
                .findAny()
                .orElseThrow(() -> {
                    log.error("No valid flavor text found");
                    return new NoValidFlavorTextException();
                })
                .flavorText()
                .replaceAll("\\p{C}", " ");
        String pokemonName = pokemonInfo.name();
        String pokemonHabitatName = pokemonInfo.habitat().name();
        Boolean isLegendaryPokemon = pokemonInfo.isLegendary();

        log.info("Retrieved {} information", name);

        return PokemonInfoResponse
                .builder()
                .name(pokemonName)
                .habitat(pokemonHabitatName)
                .description(pokemonDescription)
                .isLegendary(isLegendaryPokemon)
                .build();
    }

    private Optional<String> tryTranslateDescription(PokemonInfoResponse pokemonInfo) {
        try {
            return fetchTranslation(pokemonInfo).getBody()
                    .map(FunTranslationsResponse::contents)
                    .map(TranslationContentModel::translated);
        } catch(Exception e) {
            log.error("Something went wrong during translation: {}", e.getMessage());
        }

        return Optional.empty();
    }

    private HttpResponse<FunTranslationsResponse> fetchTranslation(PokemonInfoResponse pokemonInfo) {
        final boolean shouldUseYodaTranslation = Objects.equals(pokemonInfo.getHabitat(), "cave") || pokemonInfo.getIsLegendary();
        final TranslateRequest requestBody = new TranslateRequest(pokemonInfo.getDescription());

        return shouldUseYodaTranslation
                ? funTranslationsClient.translateYoda(requestBody)
                : funTranslationsClient.translateShakespeare(requestBody);
    }
}
