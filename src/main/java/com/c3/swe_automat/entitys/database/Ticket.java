package com.c3.swe_automat.entitys.database;

import com.c3.swe_automat.enums.Ermaeßigung;
import com.c3.swe_automat.service.AdminSettings;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

import static com.c3.swe_automat.enums.Ermaeßigung.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Ticket extends AbstractBaseEntity {

    //useless
    private String name;

    private LocalDateTime kaufDatum;

    @Enumerated(EnumType.STRING)
    private Ermaeßigung ermaeßigung;

    @ManyToOne
    private Haltestelle vonHaltestelle;

    @ManyToOne
    private Haltestelle nachHaltestelle;


    public int calculateTarifstufe() {
        int vonZone = vonHaltestelle.ticketZone;
        int nachZone = nachHaltestelle.ticketZone;
        return Math.abs(vonZone - nachZone) + 1;
    }

    public int calculatePrice() {
        switch(calculateTarifstufe()) {
            case 1:
                switch(ermaeßigung) {
                    case ERWACHSEN:
                        return AdminSettings.getAdultA();
                    case KINDER:
                        return AdminSettings.getChildA();
                    case SENIOREN:
                        return AdminSettings.getSeniorA();
                    case ERMAESSIGT:
                        return AdminSettings.getDiscountA();
                }
            case 2:
                switch(ermaeßigung) {
                    case ERWACHSEN:
                        return AdminSettings.getAdultB();
                    case KINDER:
                        return AdminSettings.getChildB();
                    case SENIOREN:
                        return AdminSettings.getSeniorB();
                    case ERMAESSIGT:
                        return AdminSettings.getDiscountB();
                }
            case 3:
                switch(ermaeßigung) {
                    case ERWACHSEN:
                        return AdminSettings.getAdultC();
                    case KINDER:
                        return AdminSettings.getChildC();
                    case SENIOREN:
                        return AdminSettings.getSeniorC();
                    case ERMAESSIGT:
                        return AdminSettings.getDiscountC();
                }
            default: throw new ArithmeticException();
        }
    }
}
