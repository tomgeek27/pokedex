package com.tommasoamadori.pokedex.client.api.funtranslations;

import com.tommasoamadori.pokedex.dto.request.funtranslations.TranslateRequest;
import com.tommasoamadori.pokedex.dto.response.funtranslations.FunTranslationsResponse;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;

@Client("funtranslations")
public interface FunTranslationsClient {

    @Post("translate/yoda")
    @Header(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_FORM_URLENCODED)
    HttpResponse<FunTranslationsResponse> translateYoda(@Body TranslateRequest body);

    @Post("translate/shakespeare")
    @Header(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_FORM_URLENCODED)
    HttpResponse<FunTranslationsResponse> translateShakespeare(@Body TranslateRequest body);

}
