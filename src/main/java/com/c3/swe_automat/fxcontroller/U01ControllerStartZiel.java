package com.c3.swe_automat.fxcontroller;


import com.c3.swe_automat.beans.Automat;
import com.c3.swe_automat.beans.NewShoppingCart;
import com.c3.swe_automat.entitys.database.Haltestelle;
import com.c3.swe_automat.entitys.database.Ticket;
import com.c3.swe_automat.enums.Ermaeßigung;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.extern.AutoCompleteTextField;
import com.c3.swe_automat.repos.HaltestelleRepo;
import com.c3.swe_automat.service.AdminSettings;
import com.c3.swe_automat.service.SceneService;
import com.c3.swe_automat.service.Watchdog;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class U01ControllerStartZiel {

    private final SceneService sceneService;
    private final Automat automat;
    private final NewShoppingCart newShoppingCart;
    private final HaltestelleRepo haltestelleRepo;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
    private boolean stationSelection = false;
    private boolean startSelected = false;
    private boolean endSelected = false;

    //Time variabels for animations
    private int fadeTime = 300;
    private int scaleTime = 200;

    //Labels for date and time
    @FXML
    public Label u01Time;
    @FXML
    public Label u01DayDate;

    //Navigation buttons
    @FXML
    public Button abbrechen;
    @FXML
    public Button weiter;
    @FXML
    public Button zurueck;

    //Elementes that are blend in/out in animations
    @FXML
    public Line trennlinie;
    @FXML
    public GridPane ticketsPane;
    @FXML
    public GridPane warenkorbPane;

    //All plus-buttons
    @FXML
    public Button btnU01AdultPlus;
    @FXML
    public Button btnU01ChildPlus;
    @FXML
    public Button btnU01SeniorPlus;
    @FXML
    public Button btnU01RedPlus;

    //All minus-buttons
    @FXML
    public Button btnU01AdultMinus;
    @FXML
    public Button btnU01ChildMinus;
    @FXML
    public Button btnU01SeniorMinus;
    @FXML
    public Button btnU01RedMinus;

    //All text-field where the ticket-price is shown
    @FXML
    public Text adultPrice;
    @FXML
    public Text childPrice;
    @FXML
    public Text seniorPrice;
    @FXML
    public Text reducedPrice;

    //All text-elements where the ticket-count is shown
    @FXML
    public Text adultCount;
    @FXML
    public Text childCount;
    @FXML
    public Text seniorCount;
    @FXML
    public Text reducedCount;

    //Text-element for total-amount
    @FXML
    Text summe;
    //Shoppingcart-VBox where all selected tickets are listed
    @FXML
    VBox shoppingcardTickets;

    //Textfield with autocompletion
    @FXML
    public AutoCompleteTextField startZiel = new AutoCompleteTextField();
    @FXML
    public AutoCompleteTextField endZiel = new AutoCompleteTextField();

    /**
     * Get all destinations from repo
     *
     * @return String-Array with all destinations
     */
    private String[] getAllHaltestellen() {

        List<Haltestelle> all = haltestelleRepo.findAll();
        String[] arr = new String[all.size()];
        int i = 0;
        for (Haltestelle h : all) {
            arr[i] = h.getName();
            i++;
        }
        return arr;
    }

    /**
     * Functionality to switch from U01 to U02 -> blending out the elements and expand used textfield
     *
     * @param useTextfield  textfield that is expanded and used for input
     * @param killTextfield textfield that is blnded out and not used
     */
    private void blendOut(AutoCompleteTextField useTextfield, AutoCompleteTextField killTextfield) {
        //Stop autocomplete and wait until textfield is fully expanded
        useTextfield.setShowContextMenu(false);

        //disable button and blend out -> turning opacity to 0
        weiter.setDisable(true);
        Animation weiterButtonAnimation = new Timeline(
                new KeyFrame(Duration.millis(fadeTime),
                        new KeyValue(weiter.opacityProperty(), 0))
        );
        weiterButtonAnimation.play();

        //List with all destinations
        List<String> haltestellen = new LinkedList<String>(Arrays.asList(getAllHaltestellen()));

        //Check if the other textfield has a destination and remove it from the autocompletion for this textfield
        if (killTextfield.getText() != null) {
            haltestellen.remove(killTextfield.getText());
            useTextfield.getEntries().clear();
            useTextfield.getEntries().addAll(haltestellen);
        }

        //Say that a station is selected, important for blending in the elements
        stationSelection = true;

        //Set the fade-animations and execute them
        FadeTransition fadeTransitionWarenkorb =
                new FadeTransition(Duration.millis(fadeTime), warenkorbPane);
        fadeTransitionWarenkorb.setFromValue(1.0f);
        fadeTransitionWarenkorb.setToValue(0.0f);

        FadeTransition fadeTransitionTickets =
                new FadeTransition(Duration.millis(fadeTime), ticketsPane);
        fadeTransitionTickets.setFromValue(1.0f);
        fadeTransitionTickets.setToValue(0.0f);

        FadeTransition fadeTransitionTrennlinie =
                new FadeTransition(Duration.millis(fadeTime), trennlinie);
        fadeTransitionTrennlinie.setFromValue(1.0f);
        fadeTransitionTrennlinie.setToValue(0.0f);

        FadeTransition fadeTransitionTextfield =
                new FadeTransition(Duration.millis(fadeTime), killTextfield);
        fadeTransitionTextfield.setFromValue(1.0f);
        fadeTransitionTextfield.setToValue(0.0f);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(
                fadeTransitionTickets,
                fadeTransitionTrennlinie,
                fadeTransitionWarenkorb,
                fadeTransitionTextfield
        );
        //Resume autocomplete after finished animation and show it if an input was made
        parallelTransition.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                useTextfield.setShowContextMenu(true);
                if (useTextfield.getText() != "")
                    useTextfield.getEntriesPopup().show(useTextfield, Side.BOTTOM, 0, 0);
            }
        });
        parallelTransition.play();

        //Disable unused textfield
        killTextfield.setDisable(true);

        //Animate and execute the expanding of the textfield
        Animation animation = new Timeline(
                new KeyFrame(Duration.millis(scaleTime),
                        new KeyValue(useTextfield.prefWidthProperty(), 1260))
        );
        animation.play();
    }

    /**
     * Initialization method, is called at the beginning
     */
    public void initialize() {
        //"weiter"-button is normally disabled, while shoppingcart is empty
        if (AdminSettings.isDebug()) {
            weiter.setDisable(false);
        }

        stationSelection = false;
        startSelected = false;
        endSelected = false;

        //Start watchdog-timer
        Watchdog.set(sceneService);
        Watchdog.reset();

        //Time thread updates Day, Date and Time every second in new Thread
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

        //Make the ticketsPane invisible by default, because start/end-destinations are empty after first init
        ticketsPane.setVisible(false);

        //Sets an event-listener on the autocompletion contextmenu, so the blend in is executed after clicking the second destination instead of waiting for a click outside the textfields
        startZiel.getEntriesPopup().setOnAction(event -> {
            stationSelection = true;
            Platform.runLater(this::textfielAction);
        });
        endZiel.getEntriesPopup().setOnAction(event -> {
            stationSelection = true;
            Platform.runLater(this::textfielAction);
        });

        //Sets shoppingcart if tickets are already in, blends in the tickets-pane and sets previous start/end destination
        refreshShoppingcartPane();
        if (newShoppingCart.getLastStartDestination() != null && newShoppingCart.getLastEndDestination() != null) {
            startZiel.setText(newShoppingCart.getLastStartDestination());
            endZiel.setText(newShoppingCart.getLastEndDestination());
            stationSelection = true;
            textfielAction();
        }
    }

    /**
     * Action that is called after pressing the "weiter"-button
     */
    public void weiterButton() {
        Watchdog.reset();

        //Save last start/end destinations into shoppingcart
        newShoppingCart.setLastStartDestination(startZiel.getText());
        newShoppingCart.setLastEndDestination(endZiel.getText());

        sceneService.startScene(SceneEnum.U03TICKETINFORMATIONEN);
    }

    /**
     * Action that is called after pressing "Zurück"-button
     */
    public void zurueckButton() {
        Watchdog.cancel();

        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

    /**
     * Action that is called after pressing "Abbrechen"-button
     */
    public void abbrechenButton() {
        Watchdog.cancel();

        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

    /**
     * Action that ist called after pressing start-destination textfield
     */
    public void startSelection() {
        Watchdog.reset();

        //Call the blend-out and mark that the start is selected
        if (!stationSelection) {
            startSelected = true;
            blendOut(startZiel, endZiel);
        }
    }

    /**
     * Action that is called after pressing end-destination textfield
     */
    public void endSelection() {
        Watchdog.reset();

        //Call the blend-out and mark that the end is selected, also starts animation to move on X-layout for smooth textfield-expansion
        if (!stationSelection) {
            endSelected = true;
            blendOut(endZiel, startZiel);

            Animation animation2 = new Timeline(
                    new KeyFrame(Duration.millis(scaleTime),
                            new KeyValue(endZiel.layoutXProperty(), 70))
            );
            animation2.play();
        }
    }

    /**
     * Action that is called for blending in the textfield again (visual change from U02 to U01)
     */
    public void textfielAction() {
        Watchdog.reset();

        if (stationSelection) {
            stationSelection = false;
            //Fade transition is used late to differentiate if start- or endtextfield has to blend in
            FadeTransition fadeTransitionTextfield = null;

            List<String> haltestellen = new LinkedList<String>(Arrays.asList(getAllHaltestellen()));
            if (haltestellen.contains(startZiel.getText()) && haltestellen.contains(endZiel.getText())) {
                Haltestelle start = null;
                Haltestelle ziel = null;
                List<Haltestelle> allHaltestellen = haltestelleRepo.findAll();

                for (Haltestelle haltestelle : allHaltestellen) {
                    if (haltestelle.getName().equals(startZiel.getText()))
                        start = haltestelle;
                    if (haltestelle.getName().equals(endZiel.getText()))
                        ziel = haltestelle;
                }

                assert start != null;
                int vonZone = start.getTicketZone();
                assert ziel != null;
                int nachZone = ziel.getTicketZone();
                int ticketZone = Math.abs(vonZone - nachZone) + 1;

                System.out.println("Von " + start.getName() + " nach " + ziel.getName() + " befindet sich in der Ticket-Zone " + ticketZone);

                //sets the ticket prices for the selected ticket-zone
                if (ticketZone == 1) {
                    adultPrice.setText(convertDoubleToAmountString((double) AdminSettings.getAdultA() / 100, 1));
                    childPrice.setText(convertDoubleToAmountString((double) AdminSettings.getChildA() / 100, 1));
                    seniorPrice.setText(convertDoubleToAmountString((double) AdminSettings.getSeniorA() / 100, 1));
                    reducedPrice.setText(convertDoubleToAmountString((double) AdminSettings.getDiscountA() / 100, 1));
                } else if (ticketZone == 2) {
                    adultPrice.setText(convertDoubleToAmountString((double) AdminSettings.getAdultB() / 100, 1));
                    childPrice.setText(convertDoubleToAmountString((double) AdminSettings.getChildB() / 100, 1));
                    seniorPrice.setText(convertDoubleToAmountString((double) AdminSettings.getSeniorB() / 100, 1));
                    reducedPrice.setText(convertDoubleToAmountString((double) AdminSettings.getDiscountB() / 100, 1));
                } else if (ticketZone == 3) {
                    adultPrice.setText(convertDoubleToAmountString((double) AdminSettings.getAdultC() / 100, 1));
                    childPrice.setText(convertDoubleToAmountString((double) AdminSettings.getChildC() / 100, 1));
                    seniorPrice.setText(convertDoubleToAmountString((double) AdminSettings.getSeniorC() / 100, 1));
                    reducedPrice.setText(convertDoubleToAmountString((double) AdminSettings.getDiscountC() / 100, 1));
                }

                ticketsPane.setVisible(true);
            } else {
                ticketsPane.setVisible(false);
            }

            if (startSelected) /*the case that start-destination textfield was used*/ {
                startSelected = false;
                startZiel.getParent().requestFocus();
                endZiel.setDisable(false);

                //Animate back to normal width
                Animation animation = new Timeline(
                        new KeyFrame(Duration.millis(scaleTime),
                                new KeyValue(startZiel.prefWidthProperty(), 590))
                );
                animation.play();

                fadeTransitionTextfield =
                        new FadeTransition(Duration.millis(fadeTime), endZiel);
                fadeTransitionTextfield.setFromValue(0.0f);
                fadeTransitionTextfield.setToValue(1.0f);
            } else if (endSelected) /*the case that end-destination textfield was used*/ {
                endSelected = false;
                endZiel.getParent().requestFocus();
                startZiel.setDisable(false);

                //Animate back to normal width
                Animation animation = new Timeline(
                        new KeyFrame(Duration.millis(scaleTime),
                                new KeyValue(endZiel.prefWidthProperty(), 590))
                );
                animation.play();

                //Animate back to normal position so the expansion looks smooth in one direction
                Animation animation2 = new Timeline(
                        new KeyFrame(Duration.millis(scaleTime),
                                new KeyValue(endZiel.layoutXProperty(), 740))
                );
                animation2.play();

                //sets the fade-in animation start (in this case the killed textfield) textfield
                fadeTransitionTextfield =
                        new FadeTransition(Duration.millis(fadeTime), startZiel);
                fadeTransitionTextfield.setFromValue(0.0f);
                fadeTransitionTextfield.setToValue(1.0f);
            }

            //setup all fade-in animations
            FadeTransition fadeTransitionWarenkorb =
                    new FadeTransition(Duration.millis(fadeTime), warenkorbPane);
            fadeTransitionWarenkorb.setFromValue(0.0f);
            fadeTransitionWarenkorb.setToValue(1.0f);

            FadeTransition fadeTransitionTickets =
                    new FadeTransition(Duration.millis(fadeTime), ticketsPane);
            fadeTransitionTickets.setFromValue(0.0f);
            fadeTransitionTickets.setToValue(1.0f);

            FadeTransition fadeTransitionTrennlinie =
                    new FadeTransition(Duration.millis(fadeTime), trennlinie);
            fadeTransitionTrennlinie.setFromValue(0.0f);
            fadeTransitionTrennlinie.setToValue(1.0f);

            //play all fade-animations
            ParallelTransition parallelTransition = new ParallelTransition();
            parallelTransition.getChildren().addAll(
                    fadeTransitionTickets,
                    fadeTransitionTrennlinie,
                    fadeTransitionWarenkorb
            );
            parallelTransition.play();

            //blends in "weiter"-button with half opacity for the case the button is still disabled
            Animation weiterButtonAnimation = new Timeline(
                    new KeyFrame(Duration.millis(fadeTime),
                            new KeyValue(weiter.opacityProperty(), 0.5))
            );
            weiterButtonAnimation.play();

            //activates the button again and returns to full opacity
            if(newShoppingCart.getTicketsList().size() > 0) {
                weiter.setDisable(false);
                weiter.setOpacity(1.0);
            }

            //also fade in killed textfield
            if (fadeTransitionTextfield != null)
                fadeTransitionTextfield.play();

            //refresh the counter for the tickets for the case user came back from U03 and start/end destinations are set from memory
            refreshButtonCount();
        }
    }

    /**
     * Handle for adding and removing buttons over the "+" and "-" buttons
     */
    public void handleShoppingButtons() {
        textfielAction();

        Haltestelle start = null;
        Haltestelle ziel = null;
        List<Haltestelle> allHaltestellen = haltestelleRepo.findAll();

        for (Haltestelle haltestelle : allHaltestellen) {
            if (haltestelle.getName().equals(startZiel.getText()))
                start = haltestelle;
            if (haltestelle.getName().equals(endZiel.getText()))
                ziel = haltestelle;
        }

        //Handling for adult tickets
        if (btnU01AdultPlus.isFocused()) {
            System.out.println("Adult plus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN);
            if (count < 99 && newShoppingCart.getTotalCount() < automat.getBank().getPapier()) {
                adultCount.setText(count + 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN, LocalDateTime.now(), Ermaeßigung.ERWACHSEN, start, ziel);
                addTicketToShoppingcard(newTicket);
            }
        }
        if (btnU01AdultMinus.isFocused()) {
            System.out.println("Adult minus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN);
            if (count > 0) {
                adultCount.setText(count - 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN, LocalDateTime.now(), Ermaeßigung.ERWACHSEN, start, ziel);
                removeTicketFromShoppingcard(newTicket);
            }
        }

        //Handling for child tickets
        if (btnU01ChildPlus.isFocused()) {
            System.out.println("Child plus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER);
            if (count < 99 && newShoppingCart.getTotalCount() < automat.getBank().getPapier()) {
                childCount.setText(count + 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER, LocalDateTime.now(), Ermaeßigung.KINDER, start, ziel);
                addTicketToShoppingcard(newTicket);
            }
        }
        if (btnU01ChildMinus.isFocused()) {
            System.out.println("Child minus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER);
            if (count > 0) {
                childCount.setText(count - 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER, LocalDateTime.now(), Ermaeßigung.KINDER, start, ziel);
                removeTicketFromShoppingcard(newTicket);
            }
        }

        //Handling for senior tickets
        if (btnU01SeniorPlus.isFocused()) {
            System.out.println("Senior plus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN);
            if (count < 99 && newShoppingCart.getTotalCount() < automat.getBank().getPapier()) {
                seniorCount.setText(count + 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN, LocalDateTime.now(), Ermaeßigung.SENIOREN, start, ziel);
                addTicketToShoppingcard(newTicket);
            }
        }
        if (btnU01SeniorMinus.isFocused()) {
            System.out.println("Senior minus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN);
            if (count > 0) {
                seniorCount.setText(count - 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN, LocalDateTime.now(), Ermaeßigung.SENIOREN, start, ziel);
                removeTicketFromShoppingcard(newTicket);
            }
        }

        //Handling for reduced tickets
        if (btnU01RedPlus.isFocused()) {
            System.out.println("Reduced plus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT);
            if (count < 99 && newShoppingCart.getTotalCount() < automat.getBank().getPapier()) {
                reducedCount.setText(count + 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT, LocalDateTime.now(), Ermaeßigung.ERMAESSIGT, start, ziel);
                addTicketToShoppingcard(newTicket);
            }
        }
        if (btnU01RedMinus.isFocused()) {
            System.out.println("Reduced minus");
            int count = 0;
            if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT) != null)
                count = newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT);
            if (count > 0) {
                reducedCount.setText(count - 1 + "");
                Ticket newTicket = new Ticket(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT, LocalDateTime.now(), Ermaeßigung.ERMAESSIGT, start, ziel);
                removeTicketFromShoppingcard(newTicket);
            }
        }
    }

    /**
     * Adds a ticket to newShoppingCart
     *
     * @param ticket that is added to newShoppingCart
     */
    private void addTicketToShoppingcard(Ticket ticket) {
        if (newShoppingCart.getTotalCount() < automat.getBank().getPapier()) {
            newShoppingCart.addTicket(ticket);
            refreshShoppingcartPane();
        }
    }

    /**
     * Removes a ticket to newShoppingCart
     *
     * @param ticket that is removed from newShoppingCart
     */
    private void removeTicketFromShoppingcard(Ticket ticket) {
        newShoppingCart.removeTicket(ticket);
        refreshShoppingcartPane();
    }

    /**
     * Function to refresh the scene-shoppingcart
     */
    private void refreshShoppingcartPane() {
        //Handling the "weiter"-button enable/disable and opacity
        if (!AdminSettings.isDebug()) {
            if (newShoppingCart.getTickets().size() > 0) {
                weiter.setDisable(false);
                //Added opacity handling, because animations work with opacity
                weiter.setOpacity(1.0);
            } else if (newShoppingCart.getTickets().size() == 0) {
                weiter.setDisable(true);
                //Added opacity handling, because animations work with opacity
                weiter.setOpacity(0.5);
            }
        }

        //clearing the shoppingcart and adding the tickets again with new correct text-values
        shoppingcardTickets.getChildren().clear();
        for (int i = 0; i < newShoppingCart.getTickets().size(); i++) {
            String key = (String) newShoppingCart.getTickets().keySet().toArray()[i];
            int value = newShoppingCart.getTickets().get(key);
            System.out.println(value);

            Ticket ticket = null;
            for (int j = 0; j < newShoppingCart.getTicketsList().size(); j++) {
                if (newShoppingCart.getTicketsList().get(j).getName().equals(key)) {
                    ticket = newShoppingCart.getTicketsList().get(j);
                }
            }

            //create the visual ticket element in the shoppingcart
            if (ticket != null) {
                Pane ticketPane = new Pane();
                ticketPane.setPrefHeight(90);

                Text ticketCount = new Text();
                ticketCount.setLayoutX(8.0);
                ticketCount.setLayoutY(64.0);
                ticketCount.setStrokeType(StrokeType.OUTSIDE);
                ticketCount.setStrokeWidth(0.0);
                ticketCount.setText(value + "x");
                ticketCount.setFont(Font.font(58.0));

                Text ticketFrom = new Text();
                ticketFrom.setLayoutX(100.0);
                ticketFrom.setLayoutY(55.0);
                ticketFrom.setStrokeType(StrokeType.OUTSIDE);
                ticketFrom.setStrokeWidth(0.0);
                ticketFrom.setWrappingWidth(236.25);
                ticketFrom.setText("Von " + ticket.getVonHaltestelle().getName());
                ticketFrom.setFont(Font.font(18.0));

                Text ticketTo = new Text();
                ticketTo.setLayoutX(100.0);
                ticketTo.setLayoutY(82.0);
                ticketTo.setStrokeType(StrokeType.OUTSIDE);
                ticketTo.setStrokeWidth(0.0);
                ticketTo.setWrappingWidth(236.25);
                ticketTo.setText("Nach " + ticket.getNachHaltestelle().getName());
                ticketTo.setFont(Font.font(18.0));

                Text ticketClass = new Text();
                ticketClass.setLayoutX(100.0);
                ticketClass.setLayoutY(32.0);
                ticketClass.setStrokeType(StrokeType.OUTSIDE);
                ticketClass.setStrokeWidth(0.0);
                ticketClass.setText(ticket.getErmaeßigung().toString());
                ticketClass.setFont(Font.font("Arial", FontWeight.BOLD, 33.0));

                Text ticketSumme = new Text();
                ticketSumme.setLayoutX(310.0);
                ticketSumme.setLayoutY(60.0);
                ticketSumme.setStrokeType(StrokeType.OUTSIDE);
                ticketSumme.setStrokeWidth(0.0);
                ticketSumme.setWrappingWidth(148.640625);
                ticketSumme.setTextAlignment(TextAlignment.RIGHT);
                ticketSumme.setText(convertDoubleToAmountString((double) ticket.calculatePrice() / 100, value));
                ticketSumme.setFont(Font.font(42.0));

                ticketPane.getChildren().add(ticketCount);
                ticketPane.getChildren().add(ticketFrom);
                ticketPane.getChildren().add(ticketTo);
                ticketPane.getChildren().add(ticketClass);
                ticketPane.getChildren().add(ticketSumme);

                shoppingcardTickets.getChildren().add(ticketPane);
            }
        }
        //calculate sum-price for all tickets and set value to UI
        summe.setText(convertDoubleToAmountString((double) newShoppingCart.calculatePriceSum() / 100, 1));
    }

    /**
     * Function to refresh the ticket-counter of adult, child, senior and reduced
     */
    private void refreshButtonCount() {
        if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN) != null)
            adultCount.setText(newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERWACHSEN) + "");
        else
            adultCount.setText("0");

        if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER) != null)
            childCount.setText(newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.KINDER) + "");
        else
            childCount.setText("0");

        if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN) != null)
            seniorCount.setText(newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.SENIOREN) + "");
        else
            seniorCount.setText("0");

        if (newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT) != null)
            reducedCount.setText(newShoppingCart.getTickets().get(startZiel.getText() + "-" + endZiel.getText() + "-" + Ermaeßigung.ERMAESSIGT) + "");
        else
            reducedCount.setText("0");
    }

    /**
     * Helping function to return the ticket price as a string
     *
     * @param amount price
     * @param count of all tickets
     * @return price string
     */
    private String convertDoubleToAmountString(double amount, int count) {
        BigDecimal bd = new BigDecimal(count * amount).setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd).replace(".", ",") + "€";
    }

    /**
     * Function to reset timer if user is making a input from their keyboard
     */
    public void onTextIput() {
        Watchdog.reset();
    }
}