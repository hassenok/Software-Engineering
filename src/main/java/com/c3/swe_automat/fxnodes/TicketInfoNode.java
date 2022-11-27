package com.c3.swe_automat.fxnodes;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.io.IOException;

public class TicketInfoNode extends HBox {
    @FXML
    public Label txtQuantity;
    @FXML
    public Text txtDiscount;
    @FXML
    public Text txtFromTo;
    @FXML
    public Text txtPrice;

    public TicketInfoNode() {
        this(1, "Erwachsene", "StartA", "StartB", "12,00€");
    }

    public TicketInfoNode(int quantity, String discountName, String from, String to, String price) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/TicketInfoNode.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        Platform.runLater(() -> {
            setQuantity(quantity);
            txtDiscount.setText(discountName);
            txtFromTo.setText("Von " + from + " nach " + to);
            txtPrice.setText(price);
        });


        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setQuantity(int quantity) {
        txtQuantity.setText(quantity + "x");
    }

}