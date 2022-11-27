package com.c3.swe_automat.fxcontroller;

import com.c3.swe_automat.entitys.database.QTicket;
import com.c3.swe_automat.entitys.database.Ticket;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.repos.TicketRepo;
import com.c3.swe_automat.service.SceneService;
import com.c3.swe_automat.service.Watchdog;
import io.github.palexdev.materialfx.controls.base.AbstractMFXDialog;
import io.github.palexdev.materialfx.controls.enums.DialogType;
import io.github.palexdev.materialfx.controls.factories.MFXAnimationFactory;
import io.github.palexdev.materialfx.controls.factories.MFXDialogFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class A02ControllerWartungStatistik implements Initializable {

    private final SceneService sceneService;
    private final TicketRepo ticketRepo;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    private final String[] days = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
    private final String[] times = {"00","01","02","03","04","05","06","07","08","09",
                                    "10","11","12","13","14","15","16","17","18","19",
                                    "20","21","22","23"};
    private final String[] months = {"Januar","Februar","März","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember"};

    @FXML public Label u01Time;
    @FXML public Label u01DayDate;
    @FXML public TextField tfTicketsSoldDay;

    @FXML
    Pane parent;
    @FXML DatePicker dateRangePickerStart;
    @FXML DatePicker dateRangePickerEnd;
    @FXML BarChart<String, Integer> barChart;
    @FXML DatePicker dpTicketsSoldDay;
    @FXML public Button abbrechen;
    @FXML public Button statistikAnsehenButton;
    @FXML public TextField lblTicketsSoldAll;

    @FXML TextField soldTicketsOnDay;
    @FXML TextField soldTicketsOnWeek;
    @FXML TextField soldTicketsOnMonth;

    @FXML ComboBox<String> yearForWeek;
    @FXML ComboBox<String> weekForWeek;
    @FXML ComboBox<String> yearForMonth;
    @FXML ComboBox<String> monthForMonth;

    private AbstractMFXDialog warningDateDialog;

    private int getSizeOfIterable(Iterable<Ticket> list) {
        List<Ticket> ticketlist = new ArrayList<>();
        list.forEach(ticketlist::add);
        return ticketlist.size();
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

        statistikAnsehenButton.setDisable(true);

        soldTicketsOnDay.setDisable(true);
        soldTicketsOnWeek.setDisable(true);
        soldTicketsOnMonth.setDisable(true);
        lblTicketsSoldAll.setDisable(true);
        lblTicketsSoldAll.setText(ticketRepo.findAll().size() + " Tickets");

        //Setup calendar-week and month picker
        weekForWeek.setDisable(true);
        monthForMonth.setDisable(true);

        for(int i = 1999; i <= 2050; i++){
            yearForWeek.getItems().add(i + "");
            yearForMonth.getItems().add(i + "");
        }
        yearForWeek.setValue("Jahr");
        yearForMonth.setValue("Jahr");
        yearForWeek.setVisibleRowCount(4);
        yearForMonth.setVisibleRowCount(4);

        //Setup date-range-picker
        dateRangePickerStart.setValue(LocalDate.now());
        refreshBarChart();
    }

    public void selectYearForWeek(){
        Watchdog.reset();
        if(!Objects.equals(yearForWeek.getValue(), "Jahr")) {
            weekForWeek.setDisable(false);
            weekForWeek.getItems().clear();
            weekForWeek.setValue("Woche");

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.parseInt(yearForWeek.getValue()));
            int numberOfWeeks = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR);

            for(int i = 1; i <= numberOfWeeks; i++){
                weekForWeek.getItems().add("KW " + i);
            }
        }
    }

    public void selectYearForMonth(){
        Watchdog.reset();
        monthForMonth.setValue("Monat");
        if(!Objects.equals(yearForMonth.getValue(), "Jahr")) {
            monthForMonth.setDisable(false);
            monthForMonth.getItems().clear();
            monthForMonth.setValue("Monat");

            for (String month : months) {
                monthForMonth.getItems().add(month);
            }

            monthForMonth.setVisibleRowCount(4);
        }
    }

    public void ticketsSoldDayClicked() {
        Watchdog.reset();
        LocalDate date = dpTicketsSoldDay.getValue();

        setTicketCount(
            LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 0, 0, 0),
            LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 23, 59, 59),
            soldTicketsOnDay
        );
    }

    public void selectWeek() {
        Watchdog.reset();
        if(weekForWeek.getSelectionModel().getSelectedItem() == null)
            return;
        String yearString = yearForWeek.getSelectionModel().getSelectedItem();
        String weekString = weekForWeek.getSelectionModel().getSelectedItem().replace("KW ", "");

        if(!yearString.equals("Jahr") && !weekString.equals("Woche") && !weekString.equals("")) {
            int year = Integer.parseInt(yearString);
            int weekNumber = Integer.parseInt(weekString);
            LocalDate date = LocalDate.of(year, Month.JANUARY, 10);
            LocalDate dayInWeek = date.with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber);
            LocalDate weekStart = dayInWeek.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = dayInWeek.with(DayOfWeek.SUNDAY);

            setTicketCount(
                    LocalDateTime.of(weekStart.getYear(), weekStart.getMonth(), weekStart.getDayOfMonth(), 0, 0, 0),
                    LocalDateTime.of(weekEnd.getYear(), weekEnd.getMonth(), weekEnd.getDayOfMonth(), 23, 59, 59),
                    soldTicketsOnWeek);
        }

    }

    public void selectMonth() {
        Watchdog.reset();
        String year = yearForMonth.getSelectionModel().getSelectedItem();
        int month = monthForMonth.getSelectionModel().getSelectedIndex();
        String monthString = monthForMonth.getSelectionModel().getSelectedItem();
        if(!year.equals("Jahr") && !monthString.equals("Monat")) {
            System.out.println("Month: " + month);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month);

            setTicketCount(
                    LocalDateTime.of(Integer.parseInt(year), month+1, 1, 0, 0, 0),
                    LocalDateTime.of(Integer.parseInt(year), month+1, cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59),
                    soldTicketsOnMonth);
        }
    }

    private void setTicketCount(LocalDateTime start, LocalDateTime end, TextField tf) {
        var list = ticketRepo.findAll(QTicket.ticket.kaufDatum.between(start, end));

        tf.setText(getSizeOfIterable(list) + " Tickets");
        tf.setDisable(true);
    }

    public void refreshBarChart(){
        Watchdog.reset();
        if(dateRangePickerEnd.getValue() != null && dateRangePickerEnd.getValue().isBefore(dateRangePickerStart.getValue())){
            warningDateDialog = MFXDialogFactory.buildDialog(DialogType.WARNING, "Warnung", "Das 'Bis'-Datum muss nach dem 'Von'-Datum sein!");
            warningDateDialog.setAnimateIn(true);
            warningDateDialog.setAnimateOut(true);
            warningDateDialog.setInAnimationType(MFXAnimationFactory.SLIDE_IN_TOP);
            warningDateDialog.setVisible(false);
            this.parent.getChildren().addAll(warningDateDialog);
            MFXDialogFactory.convertToSpecific(DialogType.WARNING, warningDateDialog);
            warningDateDialog.show();

            warningDateDialog.setLayoutX((parent.getWidth() / 2) - warningDateDialog.getPrefWidth() / 2);
            warningDateDialog.setLayoutY((parent.getHeight() / 2) - warningDateDialog.getPrefHeight() / 2);
            dateRangePickerEnd.setValue(null);
        }
        else if(dateRangePickerEnd.getValue() == null || dateRangePickerStart.getValue().equals(dateRangePickerEnd.getValue())){
            barChart.getData().clear();
            barChart.setAnimated(true);
            XYChart.Series<String,Integer> series = new XYChart.Series<>();
            series.setName("Ticketverkäufe in xx:00 Uhr - xx:59 Uhr ===> 'xx' entspricht Stunde in der Grafik");
            for (int i = 0; i < times.length; i++) {
                var list = ticketRepo.findAll(QTicket.ticket.kaufDatum.between(dateRangePickerStart.getValue().atTime(i,0), dateRangePickerStart.getValue().atTime(i,59)));
                series.getData().add(new XYChart.Data(times[i], getSizeOfIterable(list)));
            }
            barChart.getData().add(series);
            barChart.setAnimated(false);
        }
        else if (dateRangePickerEnd.getValue() != null){
            List<LocalDate> pickedDates = dateRangePickerStart.getValue().datesUntil(dateRangePickerEnd.getValue()).collect(Collectors.toCollection(LinkedList::new));
            pickedDates.add(dateRangePickerEnd.getValue());
            barChart.getData().clear();
            barChart.setAnimated(true);
            XYChart.Series<String,Integer> series = new XYChart.Series<>();
            series.setName("Ticketverkäufe an jeweiligen Tag");
            for (LocalDate pickedDate : pickedDates) {
                var list = ticketRepo.findAll(QTicket.ticket.kaufDatum.between(pickedDate.atTime(0,0), pickedDate.atTime(23,59)));
                series.getData().add(new XYChart.Data(pickedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), getSizeOfIterable(list)));
            }
            barChart.getData().add(series);
            barChart.setAnimated(false);
        }
    }

    public void resetTimer(){
        Watchdog.reset();
    }
}