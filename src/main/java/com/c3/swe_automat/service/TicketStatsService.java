package com.c3.swe_automat.service;

import com.c3.swe_automat.entitys.database.QTicket;
import com.c3.swe_automat.entitys.database.Ticket;
import com.c3.swe_automat.repos.TicketRepo;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TicketStatsService {
    private final TicketRepo ticketRepo;

    public int testStats(String vonId) {
        List<Ticket> tickets = new ArrayList<>();
        BooleanExpression eq = QTicket.ticket.vonHaltestelle.id.eq(vonId);
        ticketRepo.findAll(eq).forEach(tickets::add);
        return tickets.size();
    }

}