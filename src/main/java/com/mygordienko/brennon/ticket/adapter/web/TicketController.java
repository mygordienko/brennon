package com.mygordienko.brennon.ticket.adapter.web;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.mygordienko.brennon.ticket.TicketCreateRequest;
import com.mygordienko.brennon.ticket.TicketId;
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

    @GetMapping("/tickets/{id}")
    public String getTicket(Model model, @PathVariable long id) {
        var ticket = ticketService.getTicket(TicketId.of(id));

        var ticketDto = new TicketDto(
            ticket.getId().toString(), 
            ticket.getAuthor().id().toString(), 
            ticket.getStatus().getDisplayName(),
            ticket.getSubject(),
            ticket.getText(), 
            ticket.getAuthor().getFullName(), 
            ticket.getCreated(), 
            ticket.getUpdated(), 
            ticket.getReplies().stream()
            .map(reply -> new TicketReplyDto(
                    reply.getId().toString(), 
                    reply.getAuthor().id().toString(), 
                    reply.getText(), 
                    reply.getAuthor().getFullName(), 
                    reply.getCreated(), 
                    reply.getUpdated()))
                .toList());

        model.addAttribute("ticket", ticketDto);

        return "ticket/ticket-details";
    }


    @GetMapping("/tickets/create")
    public String createTicketForm(Model model) {
        model.addAttribute("request", new TicketCreateRequest("", ""));
        return "ticket/create-ticket";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@Validated @ModelAttribute("request") TicketCreateRequestDto request,
                               BindingResult bindingResult, Model model) {
        // TODO: validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("request", request);
            return "ticket/create-ticket";
        }

        ticketService.createTicket(request.toRequest());

        return "redirect:/tickets/my";
    }
}
