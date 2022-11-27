package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.beans.Automat;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.service.AdminSettings;
import com.c3.swe_automat.service.SceneService;
import com.c3.swe_automat.service.Watchdog;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
public class A01ControllerWartungPreise implements Initializable {

    private final SceneService sceneService;
    private final Automat automat;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    @FXML
    public Label u01Time;
    @FXML
    public Label u01DayDate;

    @FXML
    public Button abbrechen;
    @FXML
    public Button werteAnpassenButton;

    @FXML
    public TextField autoAbbruch;
    @FXML
    public TextField endScreen;
    @FXML
    public TextField access;
    @FXML
    public TextField passwort;

    @FXML
    public TextField adultA;
    @FXML
    public TextField adultB;
    @FXML
    public TextField adultC;
    @FXML
    public TextField childA;
    @FXML
    public TextField childB;
    @FXML
    public TextField childC;
    @FXML
    public TextField seniorA;
    @FXML
    public TextField seniorB;
    @FXML
    public TextField seniorC;
    @FXML
    public TextField discountA;
    @FXML
    public TextField discountB;
    @FXML
    public TextField discountC;
    @FXML
    public MFXToggleButton toggleLockAutomat;

    public void abbrechenButton(ActionEvent actionEvent) {
        Watchdog.cancel();
        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

    public void werteButton(ActionEvent actionEvent) {
        Watchdog.cancel();
        sceneService.startScene(SceneEnum.A01WARTUNG_WERTE);
    }

    public void statistikButton(ActionEvent actionEvent) {
        Watchdog.cancel();
        sceneService.startScene(SceneEnum.A02WARTUNG_STATISTIK);
    }

    public void speicherButton(ActionEvent actionEvent) {
        Watchdog.cancel();
        sceneService.startScene(SceneEnum.A03WARTUNG_SPEICHER);
    }

    public void updateSettings() {
        if (!autoAbbruch.getText().equals("")) AdminSettings.setTimeout(1000 * Integer.parseInt(autoAbbruch.getText()));
        if (!endScreen.getText().equals("")) AdminSettings.setEndscreen(1000 * Integer.parseInt(endScreen.getText()));
        if (!access.getText().equals("") && access.getText().matches("^[l|r]+$"))
            AdminSettings.setAccessCombination(access.getText());
        else
            access.setText(AdminSettings.getAccessCombination());
        if (!passwort.getText().equals(""))
            automat.updateDbAdminPassword(AdminSettings.getAdmin_username(), passwort.getText());

        automat.updateDbWithAdminSettings();
        Watchdog.reset();
    }

    public void updatePrices() {
        if (!adultA.getText().equals("") && convertPriceStringToInt(adultA.getText()) >= 0)
            AdminSettings.setAdultA(convertPriceStringToInt(adultA.getText()));
        if (!adultB.getText().equals("") && convertPriceStringToInt(adultB.getText()) >= 0)
            AdminSettings.setAdultB(convertPriceStringToInt(adultB.getText()));
        if (!adultC.getText().equals("") && convertPriceStringToInt(adultC.getText()) >= 0)
            AdminSettings.setAdultC(convertPriceStringToInt(adultC.getText()));
        if (!childA.getText().equals("") && convertPriceStringToInt(childA.getText()) >= 0)
            AdminSettings.setChildA(convertPriceStringToInt(childA.getText()));
        if (!childB.getText().equals("") && convertPriceStringToInt(childB.getText()) >= 0)
            AdminSettings.setChildB(convertPriceStringToInt(childB.getText()));
        if (!childC.getText().equals("") && convertPriceStringToInt(childC.getText()) >= 0)
            AdminSettings.setChildC(convertPriceStringToInt(childC.getText()));
        if (!seniorA.getText().equals("") && convertPriceStringToInt(seniorA.getText()) >= 0)
            AdminSettings.setSeniorA(convertPriceStringToInt(seniorA.getText()));
        if (!seniorB.getText().equals("") && convertPriceStringToInt(seniorB.getText()) >= 0)
            AdminSettings.setSeniorB(convertPriceStringToInt(seniorB.getText()));
        if (!seniorC.getText().equals("") && convertPriceStringToInt(seniorC.getText()) >= 0)
            AdminSettings.setSeniorC(convertPriceStringToInt(seniorC.getText()));
        if (!discountA.getText().equals("") && convertPriceStringToInt(discountA.getText()) >= 0)
            AdminSettings.setDiscountA(convertPriceStringToInt(discountA.getText()));
        if (!discountB.getText().equals("") && convertPriceStringToInt(discountB.getText()) >= 0)
            AdminSettings.setDiscountB(convertPriceStringToInt(discountB.getText()));
        if (!discountC.getText().equals("") && convertPriceStringToInt(discountC.getText()) >= 0)
            AdminSettings.setDiscountC(convertPriceStringToInt(discountC.getText()));

        automat.updateDbWithAdminSettings();
    }

    private int convertPriceStringToInt(String price) {
        price = price.replace(",", ".").replace("€", "");
        return (int) (Math.round(Double.parseDouble(price) * 100));
    }

    private String convertPriceIntToString(int price) {
        BigDecimal bd = BigDecimal.valueOf((double) price / 100).setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd).replace(".", ",") + "€";
    }

    public void resetTimer() {
        Watchdog.reset();
    } //fxml wouldn't accept #initialize

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Start watchdog-timer
        Watchdog.set(sceneService);
        Watchdog.reset();

        //Time thread updates Day, Date and Time every second in new Thread
        //Update day
        //Update time
        Thread timeThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    //Update day
                    u01DayDate.setText(days[LocalDate.now().getDayOfWeek().getValue() - 1] + " - " + dateFormat.format(LocalDateTime.now()));
                    //Update time
                    u01Time.setText(timeFormat.format(LocalDateTime.now()) + " Uhr");
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        timeThread.start();

        werteAnpassenButton.setDisable(true);

        //Autoabbruch- & Endscreen-zeit, Accesscombination
        autoAbbruch.setText(String.valueOf(AdminSettings.getTimeout() / 1000));
        endScreen.setText(String.valueOf(AdminSettings.getEndscreen() / 1000));
        access.setText(String.valueOf(AdminSettings.getAccessCombination()));

        //Prompttext new password
        passwort.setPromptText("User: " + AdminSettings.getAdmin_username());

        //Ticketprices
        //adults-tickets
        adultA.setText(convertPriceIntToString(AdminSettings.getAdultA()));
        adultB.setText(convertPriceIntToString(AdminSettings.getAdultB()));
        adultC.setText(convertPriceIntToString(AdminSettings.getAdultC()));

        //children-tickets
        childA.setText(convertPriceIntToString(AdminSettings.getChildA()));
        childB.setText(convertPriceIntToString(AdminSettings.getChildB()));
        childC.setText(convertPriceIntToString(AdminSettings.getChildC()));

        //senior-tickets
        seniorA.setText(convertPriceIntToString(AdminSettings.getSeniorA()));
        seniorB.setText(convertPriceIntToString(AdminSettings.getSeniorB()));
        seniorC.setText(convertPriceIntToString(AdminSettings.getSeniorC()));

        //discounted-tickets
        discountA.setText(convertPriceIntToString(AdminSettings.getDiscountA()));
        discountB.setText(convertPriceIntToString(AdminSettings.getDiscountB()));
        discountC.setText(convertPriceIntToString(AdminSettings.getDiscountC()));

        //if automat is already locked
        if (automat.isLocked())
            toggleLockAutomat.setSelected(true);
    }

    //If toggle button is clicked unlock or lock automat
    public void toggleLockStatus(ActionEvent actionEvent) {
        if (toggleLockAutomat.isSelected())
            automat.lockAutomat();
        else
            automat.unlockAutomat();

        automat.updateDbWithAdminSettings();
    }
}