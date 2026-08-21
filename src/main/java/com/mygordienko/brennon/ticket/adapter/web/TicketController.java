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

import com.mygordienko.brennon.ticket.TicketCreateCommand;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketService;

import jakarta.servlet.http.HttpServletResponse;

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
            ticket.getAuthor().getId().toString(), 
            ticket.getStatus().getDisplayName(),
            ticket.getSubject(),
            ticket.getText(), 
            ticket.getAuthor().getFullName(), 
            ticket.getCreated(), 
            ticket.getUpdated(), 
            ticket.getReplies().stream()
            .map(reply -> new TicketReplyDto(
                    reply.getId().toString(), 
                    reply.getAuthor().getId().toString(), 
                    reply.getText(), 
                    reply.getAuthor().getFullName(), 
                    reply.getCreated(), 
                    reply.getUpdated()))
                .toList());

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("replyform", new TicketReplyCommandDto());

        return "ticket/ticket-details";
    }


    @GetMapping("/tickets/create")
    public String createTicketForm(Model model) {
        model.addAttribute("request", new TicketCreateCommand("", ""));
        return "ticket/create-ticket";
    }

    @PostMapping("/tickets/create")
    public String createTicket(@Validated @ModelAttribute("request") TicketCreateCommandDto request,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("request", request);
            return "ticket/create-ticket";
        }

        ticketService.createTicket(request.toCommand());

        return "redirect:/tickets/my";
    }

    @PostMapping("/tickets/{id}/reply")
    public String replyTicket(@PathVariable int id, HttpServletResponse response, @Validated @ModelAttribute("replyform") TicketReplyCommandDto replyform,
                               BindingResult bindingResult, Model model) {

        var ticketId = TicketId.of(id);
    
        if (bindingResult.hasErrors()) {
            // Return just the fragment containing the form and errors
            // we still need ticket id to compose the submit URL 
            var ticketDto = new TicketDto(id);
            model.addAttribute("ticket", ticketDto);
            model.addAttribute("replyform", replyform);
            
            // Tell htmx to pinpoint change the form block by matching its ID directly
            // The Problem: 
            //  The default submission element targeted the general replies container 
            //  (hx-target="#reply-section"). When a validation failure occurred, the server returned an updated input form, 
            //  which got incorrectly injected inside the target area, creating duplicate, nested inputs.
            // The Solution:
            //  HX-Retarget header instructs HTMX to ignore original target specified in `hx-target` and use specified target instead
            //  We have to also instruct Thymeleaf to return corresponsing fragment (we have to wrap out form into fragment)
            response.setHeader("HX-Retarget", "#reply-form-section"); 
            return "ticket/ticket-details :: reply-form-block"; 

        }

        ticketService.replyTicket(replyform.toCommand(ticketId));

        var ticket = ticketService.getTicket(ticketId);

        var ticketDto = new TicketDto(
            ticket.getId().toString(), 
            ticket.getAuthor().getId().toString(), 
            ticket.getStatus().getDisplayName(),
            ticket.getSubject(),
            ticket.getText(), 
            ticket.getAuthor().getFullName(), 
            ticket.getCreated(), 
            ticket.getUpdated(), 
            ticket.getReplies().stream()
            .map(reply -> new TicketReplyDto(
                    reply.getId().toString(), 
                    reply.getAuthor().getId().toString(), 
                    reply.getText(), 
                    reply.getAuthor().getFullName(), 
                    reply.getCreated(), 
                    reply.getUpdated()))
                .toList());

        // Re-populate ticket data needed for the fragment layout
        model.addAttribute("ticket", ticketDto); 
        model.addAttribute("replyform", new TicketReplyCommandDto()); // Reset form
        
        return "ticket/ticket-details :: #reply-section";
    }
}
