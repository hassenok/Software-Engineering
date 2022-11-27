package com.c3.swe_automat.service;

import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StageService {
    @Getter
    @Setter
    private Stage stage;

    public Stage getActivStage() {
        return stage;
    }

    public Stage getNewStage() {
        return new Stage();
    }


}
