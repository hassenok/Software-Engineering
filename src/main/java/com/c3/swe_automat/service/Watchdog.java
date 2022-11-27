package com.c3.swe_automat.service;

import com.c3.swe_automat.enums.SceneEnum;
import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

public final class Watchdog {
    private static SceneService sceneService;
    private static Timer timer = new Timer();

    public static void reset() {
        System.out.println("Timer restarting");
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    System.out.println("platform working");
                    sceneService.startScene(SceneEnum.U00START_WINDOW);
                });
            }
        }, AdminSettings.getTimeout());
    }

    public static void cancel() {
        System.out.println("Timer cancelled");
        timer.cancel();
    }

    public static void set(SceneService _sceneService) {
        sceneService = _sceneService;
    }
}