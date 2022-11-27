package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.beans.Automat;
import com.c3.swe_automat.beans.NewShoppingCart;
import com.c3.swe_automat.entitys.database.Ticket;
import com.c3.swe_automat.enums.Coin;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.exeptions.CoinException;
import com.c3.swe_automat.exeptions.PapierExeption;
import com.c3.swe_automat.service.PdfTicketExporter;
import com.c3.swe_automat.service.SceneService;
import com.c3.swe_automat.service.Watchdog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
@RequiredArgsConstructor
public class U04ControllerBezahlen implements Initializable {

    private final SceneService sceneService;
    private final NewShoppingCart shoppingCart;
    private final Automat automat;
    private int amountToPay;
    private int amount = 0;
    private boolean bought = false;
    private List<Coin> coinsUserPayed;
    private Stage commandStage;
    TextField consoleInput;
    private VBox vbox;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    @FXML
    public Label u01Time;
    @FXML
    public Label u01DayDate;

    @FXML
    public Button abbrechenButton;
    @FXML
    public Button weiter;
    @FXML
    public Text txtPayedValue;
    @FXML
    public Text txtSum;
    @FXML
    public Text txtRemainingValue;
    @FXML
    public Text txtChangeValue;

    public void weiterButton() {
        Watchdog.cancel();
        if (commandStage != null)
            commandStage.close();
        sceneService.startScene(SceneEnum.U05ENDSCREEN);
    }

    public void abort() {
        if (coinsUserPayed != null && !coinsUserPayed.isEmpty() && !bought) {
            printToConsole(vbox, "");
            printToConsole(vbox, "Geld wird zurück gegeben:.");
            printToConsole(vbox, "");
            coinsUserPayed.forEach(c -> {
                printToConsole(vbox, c + "");
                try {
                    automat.getBank().sub(c, 1);
                } catch (CoinException ex) {
                    ex.printStackTrace();
                }
            });
            printToConsole(vbox, "");
        }

        Watchdog.reset();
        if (consoleInput != null && commandStage != null) {
            consoleInput.setDisable(true);
        }
        if ((coinsUserPayed == null || coinsUserPayed.isEmpty()) && commandStage != null) {
            commandStage.close();
        }
        coinsUserPayed = new ArrayList<>();
    }

    public void abbrechenButton() {
        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

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

        Platform.runLater(() -> {
            this.amount = 0;    //Reset payed amount -> worked for me, please fix if im wrong here [Daniel]
            coinsUserPayed = new ArrayList<>();
            amountToPay = shoppingCart.calculatePriceSum();
            if (amountToPay == 0)
                return;

            weiter.setDisable(true);
            setAmountToLabel(txtSum, amountToPay);
            calcSetRemainingValue();
            try {
                consoleWindowLogic();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        setAmountToLabel(txtPayedValue, amount);
    }

    private void consoleWindowLogic() throws IOException {
        commandStage = new Stage();
        BorderPane pane = new BorderPane();

        ScrollPane scrollPane = new ScrollPane();
        vbox = new VBox();
        pane.setCenter(scrollPane);
        scrollPane.setContent(vbox);
        scrollPane.vvalueProperty().bind(vbox.heightProperty());
        consoleInput = new TextField();
        pane.setBottom(consoleInput);

        this.amount = 0;
        setAmountToLabel(txtPayedValue, amount);

        consoleInput.setOnKeyPressed((k) -> {
            if (k.getCode() == KeyCode.ENTER) {
                Watchdog.reset();
                int input = parseConsoleInput(consoleInput.getText());

                if (input <= 0) {
                    printToConsole(vbox, "invalide input");
                    printToConsole(vbox, "to enter euro, enter: 'pay x' z.B. 'pay 5' to enter 5€");
                    printToConsole(vbox, "to enter cent, enter: 'pay cx' z.B. 'pay c50' to enter 50ct");
                    return;
                }

                System.out.println("Eingegeben: " + (Double.toString(((double) input) / 100) + " Euro eingezahlt"));

                try {
                    automat.getBank().add(input, 1);
                    coinsUserPayed.add(automat.getBank().getCoinStorage(input).getCoin());
                    amount += input;
                    printToConsole(vbox, (Double.toString(((double) input) / 100) + " Euro eingezahlt"));
                    setAmountToLabel(txtPayedValue, amount);
                } catch (CoinException e) {
                    printToConsole(vbox, "Münzspeicher voll, oder Münze darf nicht genutzt werden");
                    e.printStackTrace();
                }

                if (!calcSetRemainingValue()) {
                    abbrechenButton.setDisable(true);
                    shoppingCart.checkout();
                    bought = true;

                    consoleInput.setDisable(true);
                    consoleInput.clear();

                    setAmountToLabel(txtChangeValue, amount - amountToPay);

                    List<Coin> coins = null;
                    try {
                        coins = automat.getBank().generateChange(amount - amountToPay);

                        printToConsole(vbox, "");
                        printToConsole(vbox, "---------------------");
                        printToConsole(vbox, "Wechselgeld:");
                        printToConsole(vbox, "---------------------");
                        coins.forEach(c -> printToConsole(vbox, c + ""));
                        //print tickets to console
                        printToConsole(vbox, "");
                        printToConsole(vbox, "---------------------");
                        printToConsole(vbox, "Gekaufte Tickets:");
                        printToConsole(vbox, "---------------------");

                        for (Ticket t : shoppingCart.getTicketsList()) {
                            try {
                                automat.getBank().addPaper(-1);
                            } catch (PapierExeption e) {
                                //todo
                                e.printStackTrace();
                            }
                            String ticketInfo =
                                    "Von " + t.getVonHaltestelle().getName() +
                                            " nach " + t.getNachHaltestelle().getName() +
                                            ", Ticketklasse: " + t.getErmaeßigung() +
                                            ", Preis: " + convertDoubleToAmountString(((double) t.calculatePrice()) / 100) +
                                            ", Preisstufe: " + t.calculateTarifstufe() +
                                            ", Uhrzeit und Datum: " + t.getKaufDatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                            printToConsole(vbox, ticketInfo);
                        }

                        Button saveBtn = new Button("Tickets als PDF speichern");
                        saveBtn.setOnAction(e -> {
                            commandStage.setAlwaysOnTop(false);
                            PdfTicketExporter pdf = new PdfTicketExporter(shoppingCart.getTicketsList());
                            pdf.generatePdf();
                        });
                        printToConsole(vbox, "");
                        vbox.getChildren().add(saveBtn);
                    } catch (CoinException e) {
                        printToConsole(vbox, "");
                        printToConsole(vbox, "---------------------");
                        printToConsole(vbox, "Wechselgeld kann nicht ausgegeben werden.");
                        printToConsole(vbox, "Geld wird zurück gegeben:.");
                        printToConsole(vbox, "");
                        setAmountToLabel(txtChangeValue, amount);
                        coinsUserPayed.forEach(c -> {
                            printToConsole(vbox, c + "");
                            try {
                                automat.getBank().sub(c, 1);
                            } catch (CoinException ex) {
                                ex.printStackTrace();
                            }
                        });
                        printToConsole(vbox, "");
                        coinsUserPayed = new ArrayList<>();
                    }


                    weiter.setDisable(false);
                }
            }
        });

        commandStage.setScene(new Scene(pane));
        commandStage.setWidth(1320);
        commandStage.setHeight(350);
        commandStage.setAlwaysOnTop(true);
        commandStage.setTitle("Bezahlen: Geldeingabe");
        commandStage.setX(0);
        commandStage.setY(0);
        commandStage.show();
    }

    private void printToConsole(Pane parent, String text) {
        parent.getChildren().add(new Label(text));
    }

    private int parseConsoleInput(String text) {
        if (!text.matches("pay c?[0-9]*")) //checks that command begins with pay
            return -500;
        text = text.replace("pay ", "");

        if (text.matches("c[0-9]+")) {
            text = text.replace("c", "");
            int z = Integer.parseInt(text);
            if (z == 5 || z == 10 || z == 20 || z == 50)
                return z;
        } else if (text.matches("[0-9][0-9]?")) {
            text = text.replace("c", "");
            int z = Integer.parseInt(text);
            if (z == 1 || z == 2 || z == 5 || z == 10 || z == 20 || z == 50)
                return z * 100;
        }

        return 0;
    }

    private String convertDoubleToAmountString(double amount) {
        BigDecimal bd = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd).replace(".", ",") + "€";
    }

    private void setAmountToLabel(Text txt, double amount) {
        txt.setText(convertDoubleToAmountString(amount / 100));
    }

    /**
     * Calculate remaining value and set it to label txtRemainingValue
     *
     * @return true if var amount < amountToPay otherwise false (not enough payed)
     */
    private boolean calcSetRemainingValue() {
        double remainingValue = amountToPay - amount;
        setAmountToLabel(txtRemainingValue, remainingValue > 0 ? remainingValue : 0);
        return amount < amountToPay;
    }
}