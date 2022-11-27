package com.c3.swe_automat.beans;

import com.c3.swe_automat.entitys.database.Ticket;
import com.c3.swe_automat.repos.TicketRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Data
@RequiredArgsConstructor
public class ShoppingCart {
    public final TicketRepo ticketRepo;

    //map from Ticket to ticket count
    private HashMap<Ticket, Integer> tickets = new HashMap<>();


    /**
     * adds ticket to shoppingCart
     *
     * @return returns the amount of this Ticket after adding
     */
    public int addTicket(Ticket t) {
        if (tickets.containsKey(t)) {
            tickets.put(t, tickets.get(t) + 1);
        }
        tickets.put(t, 1);
        return tickets.get(t);
    }

    /**
     * removes ticket from shoppingCart
     *
     * @return returns the amount of this Ticket after removing
     */
    public int removeTicket(Ticket t) {
        if (tickets.containsKey(t)) {
            if (tickets.get(t) > 0) { // was > 1, but that prevents the user from buying less than 1 ticket per class. If change to 0 was incorrect, please revert back - Tarik
                tickets.put(t, tickets.get(t) - 1);
                return tickets.get(t);
            } else tickets.remove(t);
        }
        return 0;
    }


    public int calculatePriceSum() {
        AtomicInteger sum = new AtomicInteger();
        tickets.forEach((t, a) -> {
            sum.addAndGet(t.calculatePrice() * a);
        });
        return sum.intValue();
    }


    public List<Ticket> generateAllTickets() {
        List<Ticket> ts = new ArrayList<>();

        tickets.forEach((t, a) -> {
            for (int i = 0; i < a; i++) {
                ts.add(Ticket.builder()
                        .name(t.getName())
                        .nachHaltestelle(t.getNachHaltestelle())
                        .vonHaltestelle(t.getVonHaltestelle())
                        .ermaeßigung(t.getErmaeßigung())
                        .kaufDatum(LocalDateTime.now())
                        .build());
            }
        });
        return ts;
    }

    public void checkout() {
        ticketRepo.saveAll(generateAllTickets());
    }
}
