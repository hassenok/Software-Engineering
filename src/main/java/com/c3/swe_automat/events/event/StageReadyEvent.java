package com.c3.swe_automat.events.event;

import com.c3.swe_automat.enums.SceneEnum;
import javafx.stage.Stage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class  StageReadyEvent extends ApplicationEvent {
    @Getter
    private SceneEnum scene;

    @Getter
    private int width;

    @Getter
    private int height;

    public StageReadyEvent(Stage stage) {
        super(stage);
        scene = SceneEnum.U00START_WINDOW;
    }

    public StageReadyEvent(Stage stage, SceneEnum scene) {
        this(stage);
        this.scene = scene;
    }

    public StageReadyEvent(Stage stage, SceneEnum scene, int width, int height) {
        this(stage, scene);
        this.width = width;
        this.height = height;
    }

    public Stage getStage() {
        return (Stage) this.getSource();
    }
}