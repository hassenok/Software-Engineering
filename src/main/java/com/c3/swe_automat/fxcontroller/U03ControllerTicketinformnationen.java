package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.beans.NewShoppingCart;
import com.c3.swe_automat.entitys.database.Ticket;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.fxnodes.TicketInfoNode;
import com.c3.swe_automat.service.SceneService;
import com.c3.swe_automat.service.Watchdog;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class U03ControllerTicketinformnationen {

    private final SceneService sceneService;
    private final NewShoppingCart shoppingCart;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    @FXML public Label u01Time;
    @FXML public Label u01DayDate;

    @FXML
    public Button abbrechen;

    @FXML
    public Button weiter;

    @FXML
    public Button zurueck;
    @FXML
    public VBox testBox;
    @FXML
    public Label txtCompleteSum;

    public void initialize() {
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
            double completeSum = 0;
            HashMap<String, List<Ticket>> ticketMap = new HashMap<>();

            for(Ticket t : shoppingCart.getTicketsList()) {
                String name = t.getErmaeßigung() + "|" + t.getVonHaltestelle() + "|" + t.getNachHaltestelle();
                if(!ticketMap.containsKey(name)) {
                    ticketMap.put(name, new ArrayList<Ticket>());
                }
                ticketMap.get(name).add(t);
            }

            List<String> mapKeys = new ArrayList<>(ticketMap.keySet());
            Collections.sort(mapKeys);
            for(String s : mapKeys) {
                List<Ticket> list = ticketMap.get(s);
                Ticket t = list.get(0);
                double ticketCollectionSum = 0;
                for(Ticket tIt : list) {
                    ticketCollectionSum += (double) tIt.calculatePrice() / 100;
                    completeSum += (double) tIt.calculatePrice() / 100;
                }
                testBox.getChildren().add(new TicketInfoNode(list.size(), t.getErmaeßigung().toString(), t.getVonHaltestelle().getName(), t.getNachHaltestelle().getName(), convertDoubleToAmountString(ticketCollectionSum)));
            }

            txtCompleteSum.setText(convertDoubleToAmountString(completeSum));
        });
    }

    public void weiterButton() {
        Watchdog.reset();
        sceneService.startScene(SceneEnum.U04BEZAHLEN);
    }
    public void zurueckButton() {
        Watchdog.reset();
        sceneService.startScene(SceneEnum.U01START_ZIEL);
    }
    public void abbrechenButton() {
        Watchdog.cancel();
        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

    private String convertDoubleToAmountString(double amount) {
        BigDecimal bd = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd).replace(".", ",") + "€";
    }
}
