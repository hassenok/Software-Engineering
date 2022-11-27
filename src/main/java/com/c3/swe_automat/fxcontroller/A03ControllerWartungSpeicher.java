package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.beans.Automat;
import com.c3.swe_automat.entitys.database.Bank;
import com.c3.swe_automat.enums.Coin;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.exeptions.CoinException;
import com.c3.swe_automat.service.SceneService;
import com.c3.swe_automat.service.Watchdog;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
public class A03ControllerWartungSpeicher implements Initializable {

    private final SceneService sceneService;
    private final Automat automat;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    private Bank bank;

    @FXML
    public Label u01Time;
    @FXML
    public Label u01DayDate;

    @FXML
    public MFXButton abbrechen;
    @FXML
    public MFXButton btnSpeicherFuellen;
    ;

    @FXML
    public TextField ct5;

    @FXML
    public TextField ct10;

    @FXML
    public TextField ct20;

    @FXML
    public TextField ct50;

    @FXML
    public TextField ct100;

    @FXML
    public TextField ct200;

    @FXML
    public TextField ct500;

    @FXML
    public TextField ct1000;

    @FXML
    public TextField ct2000;

    @FXML
    public TextField ct5000;

    @FXML
    public TextField papierSpeicherText;


    private void refresh() {
        Watchdog.reset();

        try{
            ct5.setText(bank.getAmount(5) + "/" + bank.getCoinStorage(Coin.FUENF_CENT).getMaxAmount());
            ct10.setText(bank.getAmount(10) + "/" + bank.getCoinStorage(Coin.ZEHN_CENT).getMaxAmount());
            ct20.setText(bank.getAmount(20) + "/" + bank.getCoinStorage(Coin.ZWANZIG_CENT).getMaxAmount());
            ct50.setText(bank.getAmount(50) + "/" + bank.getCoinStorage(Coin.FUENFZIG_CENT).getMaxAmount());
            ct100.setText(bank.getAmount(100) + "/" + bank.getCoinStorage(Coin.EIN_EURO).getMaxAmount());
            ct200.setText(bank.getAmount(200) + "/" + bank.getCoinStorage(Coin.ZWEI_EURO).getMaxAmount());
            ct500.setText(bank.getAmount(500) + "/" + bank.getCoinStorage(Coin.FUENF_EURO).getMaxAmount());
            ct1000.setText(bank.getAmount(1000) + "/" + bank.getCoinStorage(Coin.ZEHN_EURO).getMaxAmount());
            ct2000.setText(bank.getAmount(2000) + "/" + bank.getCoinStorage(Coin.ZWANZIG_EURO).getMaxAmount());
            ct5000.setText(bank.getAmount(5000) + "/" + bank.getCoinStorage(Coin.FUENFZIG_EURO).getMaxAmount());
            /*TODO um Statement erweitern um maxPaper aus der Datenbank zu holen*/
            papierSpeicherText.setText(bank.getPapier() + "/1000");
        }catch(CoinException e){
            e.printStackTrace();
        }
    }


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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Start watchdog-timer
        Watchdog.set(sceneService);
        Watchdog.reset();

        //Time thread updates Day, Date and Time every second in new Thread
        //Update day
        //Update time
        bank = automat.getBank();
        refresh();

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

        btnSpeicherFuellen.setDisable(true);
        papierSpeicherText.setDisable(true);
    }


    @SneakyThrows
    public void emptyAll0005(ActionEvent actionEvent) {
        bank.fillMin(Coin.FUENF_CENT);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne0005(ActionEvent actionEvent) {
        bank.sub(Coin.FUENF_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne0005(ActionEvent actionEvent) {
        bank.add(Coin.FUENF_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll0005(ActionEvent actionEvent) {
        bank.fillMax(Coin.FUENF_CENT);
        refresh();
    }


    @SneakyThrows
    public void emptyAll0010(ActionEvent actionEvent) {
        bank.fillMin(Coin.ZEHN_CENT);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne0010(ActionEvent actionEvent) {
        bank.sub(Coin.ZEHN_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne0010(ActionEvent actionEvent) {
        bank.add(Coin.ZEHN_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll0010(ActionEvent actionEvent) {
        bank.fillMax(Coin.ZEHN_CENT);
        refresh();
    }

    @SneakyThrows
    public void emptyAll0020(ActionEvent actionEvent) {
        bank.fillMin(Coin.ZWANZIG_CENT);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne0020(ActionEvent actionEvent) {
        bank.sub(Coin.ZWANZIG_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne0020(ActionEvent actionEvent) {
        bank.add(Coin.ZWANZIG_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll0020(ActionEvent actionEvent) {
        bank.fillMax(Coin.ZWANZIG_CENT);
        refresh();
    }

    @SneakyThrows
    public void emptyAll0050(ActionEvent actionEvent) {
        bank.fillMin(Coin.FUENFZIG_CENT);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne0050(ActionEvent actionEvent) {
        bank.sub(Coin.FUENFZIG_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne0050(ActionEvent actionEvent) {
        bank.add(Coin.FUENFZIG_CENT, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll0050(ActionEvent actionEvent) {
        bank.fillMax(Coin.FUENFZIG_CENT);
        refresh();
    }

    @SneakyThrows
    public void emptyAll0100(ActionEvent actionEvent) {
        bank.fillMin(Coin.EIN_EURO);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne0100(ActionEvent actionEvent) {
        bank.sub(Coin.EIN_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne0100(ActionEvent actionEvent) {
        bank.add(Coin.EIN_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll0100(ActionEvent actionEvent) {
        bank.fillMax(Coin.EIN_EURO);
        refresh();
    }

    @SneakyThrows
    public void emptyAll0200(ActionEvent actionEvent) {
        bank.fillMin(Coin.ZWEI_EURO);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne0200(ActionEvent actionEvent) {
        bank.sub(Coin.ZWEI_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne0200(ActionEvent actionEvent) {
        bank.add(Coin.ZWEI_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll0200(ActionEvent actionEvent) {
        bank.fillMax(Coin.ZWEI_EURO);
        refresh();
    }

    @SneakyThrows
    public void emptyAll0500(ActionEvent actionEvent) {
        bank.fillMin(Coin.FUENF_EURO);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne0500(ActionEvent actionEvent) {
        bank.sub(Coin.FUENF_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne0500(ActionEvent actionEvent) {
        bank.add(Coin.FUENF_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll0500(ActionEvent actionEvent) {
        bank.fillMax(Coin.FUENF_EURO);
        refresh();
    }

    @SneakyThrows
    public void emptyAll1000(ActionEvent actionEvent) {
        bank.fillMin(Coin.ZEHN_EURO);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne1000(ActionEvent actionEvent) {
        bank.sub(Coin.ZEHN_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne1000(ActionEvent actionEvent) {
        bank.add(Coin.ZEHN_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll1000(ActionEvent actionEvent) {
        bank.fillMax(Coin.ZEHN_EURO);
        refresh();
    }

    @SneakyThrows
    public void emptyAll2000(ActionEvent actionEvent) {
        bank.fillMin(Coin.ZWANZIG_EURO);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne2000(ActionEvent actionEvent) {
        bank.sub(Coin.ZWANZIG_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne2000(ActionEvent actionEvent) {
        bank.add(Coin.ZWANZIG_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll2000(ActionEvent actionEvent) {
        bank.fillMax(Coin.ZWANZIG_EURO);
        refresh();
    }

    @SneakyThrows
    public void emptyAll5000(ActionEvent actionEvent) {
        bank.fillMin(Coin.FUENFZIG_EURO);
        refresh();
    }

    @SneakyThrows
    public void reduceByOne5000(ActionEvent actionEvent) {
        bank.sub(Coin.FUENFZIG_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void increaseByOne5000(ActionEvent actionEvent) {
        bank.add(Coin.FUENFZIG_EURO, 1);
        refresh();
    }

    @SneakyThrows
    public void fillAll5000(ActionEvent actionEvent) {
        bank.fillMax(Coin.FUENFZIG_EURO);
        refresh();
    }

    public void removeAllPaper(ActionEvent actionEvent) {
        bank.setPaperMin();
        refresh();
    }

    @SneakyThrows
    public void removeOnePaper(ActionEvent actionEvent) {
        bank.addPaper(-1);
        refresh();
    }

    @SneakyThrows
    public void increaseOnePaper(ActionEvent actionEvent) {
        bank.addPaper(1);
        refresh();
    }

    public void fillAllPaper(ActionEvent actionEvent) {
        bank.setPaperMax();
        refresh();
    }
}