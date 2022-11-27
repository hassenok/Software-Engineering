package com.c3.swe_automat.service;

import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.events.event.StageReadyEvent;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SceneService {
    private final StageService stageService;
    private final ApplicationContext context;


    public void startScene(SceneEnum scene) {
        startScene(scene, stageService.getActivStage(), 0, 0);
    }

    public void startScene(SceneEnum scene, int width, int height) {
        startScene(scene, stageService.getActivStage(), width, height);
    }

    public void startScene(SceneEnum scene, Stage stage) {
        startScene(scene, stage, 0, 0);
    }

    public void startScene(SceneEnum scene, Stage stage, int width, int height) {
        context.publishEvent(new StageReadyEvent(stage, scene, width, height));
    }

    public void startSceneInNewWindow(SceneEnum scene) {
        startSceneInNewWindow(scene, 0, 0);
    }

    public void startSceneInNewWindow(SceneEnum scene, int width, int height) {
        startScene(scene, stageService.getNewStage(), width, height);
    }

}
