package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.beans.Automat;
import com.c3.swe_automat.beans.NewShoppingCart;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.events.event.StageReadyEvent;
import com.c3.swe_automat.service.AdminSettings;
import com.c3.swe_automat.service.StageService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class U00UserStartseite {

    private final NewShoppingCart newShoppingCart;
    private final ApplicationContext context;
    private final Automat automat;
    private final StageService stageService;
    private Thread timeThread;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
    private String secretCode;
    private String secretCodeInput = "";

    @FXML
    public Button ticketKaufenButtonb2;
    @FXML
    public Label u00Time;
    @FXML
    public Label u00DayDate;
    @FXML
    public Label lblAusserBetrieb;
    @FXML
    public VBox middleBox;


    // handle data once the fields are injected
    public void initialize() {
        newShoppingCart.clear();

        ticketKaufenButtonb2.setVisible(!automat.isLocked());
        secretCode = AdminSettings.getAccessCombination();
        secretCodeInput = "";

        if (automat.getBank().isInCoinPuffer() && automat.getBank().isInPaperPuffer()) {
            automat.setLocked(true);
            lblAusserBetrieb.setText("Automat außer Betrieb! (Münzen/Papier)");
        } else if (automat.getBank().isInCoinPuffer()) {
            automat.setLocked(true);
            lblAusserBetrieb.setText("Automat außer Betrieb! (Münzen)");
        } else if (automat.getBank().isInPaperPuffer()) {
            automat.setLocked(true);
            lblAusserBetrieb.setText("Automat außer Betrieb! (Papier)");
        }

        //Umsetzung Lockscreen

        if (automat.isLocked())
            middleBox.getChildren().remove(ticketKaufenButtonb2);
        else
            middleBox.getChildren().remove(lblAusserBetrieb);


        //Time thread updates Day, Date and Time every second in new Thread
        timeThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    //Update day
                    u00DayDate.setText(days[LocalDate.now().getDayOfWeek().getValue() - 1] + " - " + dateFormat.format(LocalDateTime.now()));
                    //Update time
                    u00Time.setText(timeFormat.format(LocalDateTime.now()) + " Uhr");
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timeThread.start();
    }

    public void u01ButtonPressed(ActionEvent actionEvent) {
        timeThread.stop();
        Stage stage = (Stage) u00Time.getScene().getWindow();
        stageService.setStage(stage);
        context.publishEvent(new StageReadyEvent(stage, SceneEnum.U01START_ZIEL));
    }

    /**
     * Is called after leftSecretButton is clicked
     *
     * @param actionEvent ActionEvent of the button click
     */
    public void leftSecretButtonPressed(ActionEvent actionEvent) {
        secretCodeInput += 'l';
        if (checkIfInputClear())
            return;
        checkSecredCode();
    }

    /**
     * Is called after rightSecretButton is clicked
     *
     * @param actionEvent ActionEvent of the button click
     */
    public void rightSecretButtonPressed(ActionEvent actionEvent) {
        secretCodeInput += 'r';
        if (checkIfInputClear())
            return;
        checkSecredCode();
    }

    /**
     * checks if the input is the same as the provided code in secretCode
     */
    private void checkSecredCode() {
        int maxPos = secretCodeInput.length() - 1;
        //works because of the SCE with maxPos < secretCode.length
        if (maxPos < secretCode.length() && secretCode.charAt(maxPos) == secretCodeInput.charAt(maxPos)) {
            //if input matches secret code stopTime thread and switch Stage
            if (secretCodeInput.equals(secretCode)) {
                timeThread.stop();
                Stage stage = (Stage) u00Time.getScene().getWindow();
                stageService.setStage(stage);
                context.publishEvent(new StageReadyEvent(stage, SceneEnum.A00ADMIN_LOGIN));
            }
        }
    }

    /**
     * Checks if the input has to be cleared
     *
     * @return if input was cleared: true, another cases: false
     */
    private boolean checkIfInputClear() {
        int maxPos = secretCodeInput.length() - 1;
        //if input is longer as secretCode
        if (maxPos >= secretCode.length()) {
            secretCodeInput = "";
            return true;
        }
        //if lastCharacter is not equal to char in secretCode at same position
        if (secretCodeInput.charAt(maxPos) != secretCode.charAt(maxPos)) {
            secretCodeInput = "";
            return true;
        }
        return false;
    }
}
