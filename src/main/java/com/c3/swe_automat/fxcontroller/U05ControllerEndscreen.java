package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.service.AdminSettings;
import com.c3.swe_automat.service.SceneService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

@Component
@RequiredArgsConstructor
public class U05ControllerEndscreen implements Initializable {

    private final SceneService sceneService;

    private Timer timer;

    @FXML
    public Button abbrechen;

    @FXML
    public Button weiter;

    @FXML
    public Button zurueck;

    public void startButton() {
        timer.cancel();
        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("TIMER INITIALIZED");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    sceneService.startScene(SceneEnum.U00START_WINDOW);
                });
            }
        }, AdminSettings.getEndscreen());
    }
}
