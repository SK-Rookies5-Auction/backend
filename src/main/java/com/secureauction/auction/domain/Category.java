package com.secureauction.auction.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    DIGITAL_DEVICES("Digital Devices"),
    HOME_APPLIANCES("Home Appliances"),
    FURNITURE_INTERIOR("Furniture/Interior"),
    CLOTHING("Clothing"),
    BEAUTY_PERSONAL_CARE("Beauty/Personal Care"),
    SPORTS_LEISURE("Sports/Leisure"),
    GAMES_HOBBIES("Games/Hobbies"),
    BOOKS_TICKETS("Books/Tickets"),
    OTHER("Other");

    private final String value;
    Category(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
