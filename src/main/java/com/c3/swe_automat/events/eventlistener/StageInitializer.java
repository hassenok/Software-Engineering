package com.c3.swe_automat.events.eventlistener;

import com.c3.swe_automat.events.event.StageReadyEvent;
import com.c3.swe_automat.fxcontroller.U04ControllerBezahlen;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final ApplicationContext applicationContext;
    private final ResourceLoader resourceLoader;
    private U04ControllerBezahlen u04ControllerBezahlen;

    @Value("${spring.application.ui.titel}")
    private String title;

    @Value("${spring.application.ui.width}")
    private int width;

    @Value("${spring.application.ui.height}")
    private int height;


    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {

            if (u04ControllerBezahlen != null) {
                u04ControllerBezahlen.abort();
                u04ControllerBezahlen = null;
            }

            FXMLLoader fxmlLoader = new FXMLLoader(resourceLoader.getResource(event.getScene().sceneTemplate).getURL());
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();

            //used to return money in U04 when Watchdog resets scene
            Object controller = fxmlLoader.getController();
            if (controller instanceof U04ControllerBezahlen) {
                u04ControllerBezahlen = ((U04ControllerBezahlen) controller);
            }

            Stage stage = event.getStage();
            stage.setTitle(title);
            if (event.getWidth() > 0 && event.getHeight() > 0) {
                stage.setScene(new Scene(parent, event.getWidth(), event.getHeight()));
            } else {
                stage.setScene(new Scene(parent, width, height));
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
