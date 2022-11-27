package com.c3.swe_automat.service;

import com.c3.swe_automat.entitys.database.Ticket;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class PdfTicketExporter {
    private List<Ticket> tickets;

    public PdfTicketExporter() {
        tickets = new ArrayList<>();
    }

    public PdfTicketExporter(Ticket ticket) {
        this();
        this.tickets.add(ticket);
    }

    public PdfTicketExporter(List<Ticket> tickets) {
        this.tickets = new ArrayList<>(tickets);
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
    }

    public void generatePdf() {

        try {
            String path = getFilePath();
            if(path.equals(""))
                return;

            PDDocument doc = new PDDocument();

            for(Ticket t : this.tickets)
                doc.addPage(addPageWithTicket(doc, t.getErmaeßigung().name(), t.getVonHaltestelle().getName(), t.getNachHaltestelle().getName(), t.getKaufDatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " Uhr", convertDoubleToAmountString(t.calculatePrice()), String.valueOf(t.calculateTarifstufe())));
            doc.save(path);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    private String getFilePath() {
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF-Datei (*.pdf)", "*.pdf");
        FileChooser fc = new FileChooser();
        fc.setTitle("Ticket-PDF speichern");
        fc.getExtensionFilters().add(extFilter);

        File file = fc.showSaveDialog(new Stage());
        if(file != null) {
            return file.getAbsolutePath();
        }
        return "";
    }

    private PDPage addPageWithTicket(PDDocument doc, String tcktPriceClass, String tcktFrom, String tcktTo, String tcktDate, String tcktPrice, String tcktPriceZone) {
        PDPage myPage = new PDPage(PDRectangle.A6);

        try (PDPageContentStream cont = new PDPageContentStream(doc, myPage)) {
            PDFont font = PDType1Font.HELVETICA_BOLD;
            int fontSize = 20;

            byte[] fileContent = Base64.getDecoder().decode(new String("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").getBytes(StandardCharsets.UTF_8));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, fileContent, "DVB Picture");

            cont.drawImage(pdImage, (myPage.getMediaBox().getWidth()/2)- (float) pdImage.getWidth()/2, 300);
            cont.beginText();

            cont.setFont(font, fontSize);
            cont.setLeading(14.5f);

            String title = "DVB Ticket";
            float titleWidth = font.getStringWidth(title) / 1000 * fontSize;
            cont.newLineAtOffset((myPage.getMediaBox().getWidth() - titleWidth) / 2, 300);
            String line = "DVB Ticket";
            cont.showText(line);

            fontSize = 12;
            font = PDType1Font.HELVETICA;
            cont.setFont(PDType1Font.HELVETICA, 15);
            int offset = -15;

            cont.newLineAtOffset(-((myPage.getMediaBox().getWidth() - titleWidth) / 2)+30,-20);
            cont.newLine();
            cont.showText("Ticket: " + tcktPriceClass);

            cont.setFont(PDType1Font.HELVETICA, 12);
            cont.newLine();
            cont.newLine();
            cont.showText("Von:          " + tcktFrom);

            cont.newLine();
            cont.showText("Nach:        " + tcktTo);

            cont.newLine();
            cont.newLine();
            cont.newLine();
            cont.showText("Preisstufe: " + tcktPriceZone);

            cont.newLine();
            cont.showText("Preis:         " + tcktPrice);

            cont.newLine();
            cont.showText("Kauf:          " + tcktDate);

            title = "___________________________";
            titleWidth = font.getStringWidth(title) / 1000 * fontSize;
            cont.newLineAtOffset(((myPage.getMediaBox().getWidth() - titleWidth) / 2-35),-100);
            cont.showText(title);

            cont.newLineAtOffset((myPage.getMediaBox().getWidth() - titleWidth) / 2 - 10, 0);
            cont.showText("Hier entwerten");

            cont.endText();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return myPage;
    }
    private String convertDoubleToAmountString(double amount) {
        amount /= 100;
        BigDecimal bd = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        return String.valueOf(bd).replace(".", ",") + "€";
    }
}
