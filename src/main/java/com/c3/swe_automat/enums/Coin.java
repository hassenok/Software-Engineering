package com.c3.swe_automat.enums;

/**
 * Just a Enum to have name and value in cent of each possible coin together
 */
public enum Coin {
    EIN_CENT(1),
    ZWEI_CENT(2),
    FUENF_CENT(5),
    ZEHN_CENT(10),
    ZWANZIG_CENT(20),
    FUENFZIG_CENT(50),
    EIN_EURO(100),
    ZWEI_EURO(200),
    FUENF_EURO(500),
    ZEHN_EURO(1000),
    ZWANZIG_EURO(2000),
    FUENFZIG_EURO(5000);

    public final int value;

    Coin(int value) {
        this.value = value;
    }
}
