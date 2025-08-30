package com.example.sellify.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    CLOTHING("Kiyim-kechak"),
    ELECTRONICS("Elektronika"),
    FOOD("Oziq-ovqat"),
    FURNITURE("Mebel"),
    OTHER("boshqa");

    private final String uzName;
}
