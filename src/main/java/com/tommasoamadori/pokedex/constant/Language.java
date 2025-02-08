package com.tommasoamadori.pokedex.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    EN("en");

    private final String code;
}
