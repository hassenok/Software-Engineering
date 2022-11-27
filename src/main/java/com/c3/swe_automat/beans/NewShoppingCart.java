package com.c3.swe_automat.beans;

import com.c3.swe_automat.entitys.database.Ticket;
import com.c3.swe_automat.repos.TicketRepo;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@Data
@RequiredArgsConstructor
public class NewShoppingCart {
    public final TicketRepo ticketRepo;

    @Getter
    @Setter
    private String lastStartDestination;
    @Getter
    @Setter
    private String lastEndDestination;

    //map from Ticket to ticket count
    private List<Ticket> ticketsList = new ArrayList<>();
    private HashMap<String, Integer> tickets = new LinkedHashMap<>();

    public int getTicketCount(Ticket t) {
        return tickets.get(t.getName());
    }

    public void clear() {
        lastStartDestination = null;
        lastEndDestination = null;

        ticketsList.clear();
        tickets.clear();
    }

    public void addTicket(Ticket t) {
        ticketsList.add(t);

        if (tickets.containsKey(t.getName())) {
            tickets.put(t.getName(), tickets.get(t.getName()) + 1);
        } else {
            tickets.put(t.getName(), 1);
        }
    }

    public void removeTicket(Ticket t) {
        if (tickets.containsKey(t.getName())) {
            if (tickets.get(t.getName()) > 1) {
                tickets.put(t.getName(), tickets.get(t.getName()) - 1);
            } else if (tickets.get(t.getName()) == 1) {
                tickets.remove(t.getName());
            }
        }

        for (int i = 0; i < ticketsList.size(); i++) {
            if (ticketsList.get(i).getName().equals(t.getName())) {
                ticketsList.remove(i);
                return;
            }
        }
    }

    public int calculatePriceSum() {
        int sum = 0;
        for (Ticket ticket : ticketsList) {
            sum += ticket.calculatePrice();
        }
        return sum;
    }

//    public List<Ticket> generateAllTickets() {
//        List<Ticket> ts = new ArrayList<>();
//
//        tickets.forEach((t, a) -> {
//            for (int i = 0; i < a; i++) {
//                ts.add(Ticket.builder()
//                        .name(t.getName())
//                        .nachHaltestelle(t.getNachHaltestelle())
//                        .vonHaltestelle(t.getVonHaltestelle())
//                        .ermaeßigung(t.getErmaeßigung())
//                        .kaufDatum(LocalDateTime.now())
//                        .build());
//            }
//        });
//        return ts;
//    }

    public void checkout() {
        ticketRepo.saveAll(ticketsList);
    }

    public int getTotalCount() {
        return ticketsList.size();
    }
}
