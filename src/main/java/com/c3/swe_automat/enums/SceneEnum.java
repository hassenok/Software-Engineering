package com.c3.swe_automat.enums;


public enum SceneEnum {
    U00START_WINDOW("classpath:/templates/u00UserStartseite.fxml"),
    U01START_ZIEL("classpath:/templates/u01StartZielUebersicht.fxml"),
    U03TICKETINFORMATIONEN("classpath:/templates/u03Ticketinformationen.fxml"),
    U04BEZAHLEN("classpath:/templates/u04Bezahlen.fxml"),
    U05ENDSCREEN("classpath:/templates/u05Endscreen.fxml"),
    A00ADMIN_LOGIN("classpath:/templates/a00AdminLogin.fxml"),
    A01WARTUNG_WERTE("classpath:/templates/a01WartungWerte.fxml"),
    A02WARTUNG_STATISTIK("classpath:/templates/a02WartungStatistik.fxml"),
    A03WARTUNG_SPEICHER("classpath:/templates/a03WartungSpeicher.fxml"),
    A00ADMINISTRATOR_LOGIN("classpath:/templates/a00AdministratorLogin.fxml");


    public final String sceneTemplate;

    SceneEnum(String sceneTemplate) {
        this.sceneTemplate = sceneTemplate;
    }


}
