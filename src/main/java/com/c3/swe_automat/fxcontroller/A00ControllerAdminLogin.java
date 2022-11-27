package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.entitys.database.AdminAccount;
import com.c3.swe_automat.entitys.database.QAdminAccount;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.repos.AdminAccountRepo;
import com.c3.swe_automat.service.AdminSettings;
import com.c3.swe_automat.service.SceneService;
import com.c3.swe_automat.service.Watchdog;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.base.AbstractMFXDialog;
import io.github.palexdev.materialfx.controls.enums.DialogType;
import io.github.palexdev.materialfx.controls.factories.MFXAnimationFactory;
import io.github.palexdev.materialfx.controls.factories.MFXDialogFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
public class A00ControllerAdminLogin implements Initializable {

    private final SceneService sceneService;
    private final AdminAccountRepo adminAccountRepo;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    private boolean errorOpened = false;

    @FXML public Label u01Time;
    @FXML public Label u01DayDate;

    @FXML public Button continueWithoutLoginButton;
    @FXML public Pane lowerControlPane;
    @FXML
    Button abbrechen;
    @FXML
    Button login;
    @FXML
    TextField benutzername;
    @FXML
    PasswordField passwort;
    @FXML
    Pane pane;

    private AbstractMFXDialog errorDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Time thread updates Day, Date and Time every second in new Thread
        //Update day
        //Update time

        errorOpened = false;
        if(!AdminSettings.isDebug())
            lowerControlPane.getChildren().removeAll(continueWithoutLoginButton);
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

        errorDialog = MFXDialogFactory.buildDialog(DialogType.ERROR, "", "Login nicht erfolgreich:\n" +
                "Nutzername oder Passwort nicht korrekt / gültig.");
        errorDialog.setAnimateIn(true);
        errorDialog.setAnimateOut(true);
        errorDialog.setInAnimationType(MFXAnimationFactory.SLIDE_IN_TOP);
//        errorDialog.setOutAnimationType(MFXAnimationFactory.SLIDE_OUT_RIGHT);
        errorDialog.setVisible(false);

        Platform.runLater(() -> {
            Watchdog.set(sceneService);
            Watchdog.reset();
            this.pane.getChildren().addAll(errorDialog);
        });
    }

    public void abbrechenButton(ActionEvent actionEvent) {
        Watchdog.cancel();
        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

    @FXML
    public void loginButton(ActionEvent actionEvent) {
        Watchdog.reset();
        handleLogin();
    }

    public void loginKeyPressed(KeyEvent keyEvent) {
        Watchdog.reset();

        if(keyEvent.getCode() != KeyCode.ENTER) return;
        if(!errorOpened) {
            handleLogin();
            return;
        }

        errorDialog.close();
    }

    private void handleLogin() {
        String usr = this.benutzername.getText();
        String pwd = this.passwort.getText();

        List<AdminAccount> admins = new ArrayList<>();
        BooleanExpression eq = QAdminAccount.adminAccount.username.eq(usr);
        adminAccountRepo.findAll(eq).forEach(admins::add);

        System.out.println("usr: " + usr + ", pwd: " + pwd);
        System.out.println("Anzahl User: " + admins.size());
        System.out.println("-------------");

        if(admins.size() > 0 && admins.get(0).getPassword().equals(pwd)) {
            AdminSettings.setAdmin_username(usr);
            sceneService.startScene(SceneEnum.A01WARTUNG_WERTE);
            return;
        }


        if(errorOpened) return;

        if(!benutzername.isFocused() && !passwort.isFocused())
            benutzername.requestFocus();

        errorOpened = true;
        MFXDialogFactory.convertToSpecific(DialogType.ERROR, errorDialog);
        errorDialog.setTitle("Login nicht erfolgreich!");
        errorDialog.show();
        errorDialog.setOnBeforeClose(event -> {
            errorOpened = false;
        });
    }

    public void continueWithoutLogin(ActionEvent actionEvent) {
        Watchdog.reset();
        sceneService.startScene(SceneEnum.A01WARTUNG_WERTE);
    }
}