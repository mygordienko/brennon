package com.mygordienko.brennon.ticket.adapter.web;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mygordienko.brennon.ticket.TicketService;

@Controller
public class TicketController {
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    private final TicketService ticketService;

    @GetMapping("/tickets/my")
    public String getMyTickets(Model model, @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = ticketService.getMyTickets(pageable);

        var ticketsDtos = page.getContent().stream()
        .map(ticket -> new TicketRecordDto(
            ticket.getId().toString(),
            ticket.getStatus().getDisplayName(),
            ticket.getSubject(),
            ticket.getAuthor().getFullName(),
            ticket.getCreated(),
            ticket.getUpdated()
        ))
        .toList();


        model.addAttribute("tickets", ticketsDtos);
        model.addAttribute("page", new PageInfoDto(page.getNumber(), page.getNumberOfElements(), page.getTotalPages(), page.getSize()));

        return "ticket/ticket-list";
    }
}
