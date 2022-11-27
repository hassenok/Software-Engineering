package com.c3.swe_automat.service;

import lombok.Getter;
import lombok.Setter;

public final class AdminSettings {
    @Getter
    @Setter
    private static int timeout = 180000;
    @Getter
    @Setter
    private static int endscreen = 7000;
    @Getter
    @Setter
    private static String accessCombination = "lrl";
    @Getter
    @Setter
    private static String admin_username = "";

    //arrays would be prettier, but it works
    @Getter
    @Setter
    private static int adultA = 0;
    @Getter
    @Setter
    private static int adultB = 0;
    @Getter
    @Setter
    private static int adultC = 0;
    @Getter
    @Setter
    private static int childA = 0;
    @Getter
    @Setter
    private static int childB = 0;
    @Getter
    @Setter
    private static int childC = 0;
    @Getter
    @Setter
    private static int seniorA = 0;
    @Getter
    @Setter
    private static int seniorB = 0;
    @Getter
    @Setter
    private static int seniorC = 0;
    @Getter
    @Setter
    private static int discountA = 0;
    @Getter
    @Setter
    private static int discountB = 0;
    @Getter
    @Setter
    private static int discountC = 0;

    @Getter
    @Setter
    private static boolean locked = false;

    //ONLY WITH SOURCECODE NOT IN PROGRAM!!!
    @Getter
    @Setter
    private static boolean debug = false;
}
