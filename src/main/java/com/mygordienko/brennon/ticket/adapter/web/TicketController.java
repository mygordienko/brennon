package com.mygordienko.brennon.ticket.adapter.web;

import java.time.ZoneId;
import java.util.Arrays;

import org.springframework.context.MessageSource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.mygordienko.brennon.core.common.adapter.HtmxClientEventTriggerHeaderBuilder;
import com.mygordienko.brennon.ticket.MissingTicketReplyException;
import com.mygordienko.brennon.ticket.TicketConcurrentModificationException;
import com.mygordienko.brennon.ticket.TicketCreateCommand;
import com.mygordienko.brennon.ticket.TicketDetailsView;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;
import com.mygordienko.brennon.ticket.TicketService;
import com.mygordienko.brennon.ticket.TicketStatus;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class TicketController {
    public TicketController(TicketService ticketService, MessageSource messageSource) {
        this.ticketService = ticketService;
        this.messageSource = messageSource;
    }

    private final TicketService ticketService;
    private final MessageSource messageSource;

    @GetMapping("/tickets/my")
    public String getMyTickets(
        @CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr,
        HttpServletResponse response,
        @RequestHeader(value = "HX-Request", required = false) String hxRequest,
        @Validated @ModelAttribute("ticketRequestFilter") TicketRequestFilterDto filterDto,
        BindingResult bindingResult,
        Model model, @SortDefault(sort = "id", direction = Sort.Direction.DESC) @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        var statuses = Arrays.stream(TicketStatus.values())
            .map(s -> new TicketStatusDto(s.getCode(), s.getDisplayName()))
            .toList();
        
        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Wrong filter form input");
        }

        var filter = filterDto.toDomain();

        var page = ticketService.getMyTickets(filter, pageable);
        var zoneId = ZoneId.of(zoneIdStr);

        var ticketsDtos = page.getContent().stream()
        .map(ticket -> new TicketRecordDto(
            ticket.getId().toString(),
            ticket.getVersion(),
            ticket.getStatus().getDisplayName(),
            ticket.getSubject(),
            ticket.getAuthor().getFullName(),
            ticket.getCreated().atZoneSameInstant(zoneId).toOffsetDateTime(),
            ticket.getUpdated().atZoneSameInstant(zoneId).toOffsetDateTime()
        ))
        .toList();

        var order = page.getSort().stream()
            .findAny()
            .orElseThrow();

        var orderDto = new PageInfoDto.OrderDto(order.getProperty(), order.getDirection().name());

        model.addAttribute("tickets", ticketsDtos);
        model.addAttribute("ticketRequestFilter", filterDto);
        model.addAttribute("statuses", statuses);
        model.addAttribute("page", new PageInfoDto(page.getNumber(), page.getNumberOfElements(), page.getTotalPages(), page.getSize(), orderDto));

        if (hxRequest != null) {
            // this is a HTMX request, serve only fragment
            return "ticket/ticket-list :: page-content-block";
        } else {
            // regular request, serve full template
            return "ticket/ticket-list";
        }
    }

    @GetMapping("/tickets/{id}")
    public String getTicket(
        @CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr,
        @RequestHeader(value = "HX-Request", required = false) String hxRequest,
        Model model, 
        @PathVariable long id) {
        var zoneId = ZoneId.of(zoneIdStr);
        var ticket = ticketService.getTicket(TicketId.of(id));

        var ticketDto = composeDto(ticket, zoneId);

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion()));
        model.addAttribute("closeForm", new TicketCloseCommandDto(ticket.getVersion()));

        if (hxRequest != null) {
            // this is a HTMX request, serve only fragment
            return "ticket/ticket-details :: page-content-block";
        } else {
            // regular request, serve full template
            return "ticket/ticket-details";
        }
    }

    @GetMapping("/tickets/{id}/edit")
    public String getTicketDetailsEditFragment(@CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr, Model model, @PathVariable long id) {
        var zoneId = ZoneId.of(zoneIdStr);
        var ticket = ticketService.getTicket(TicketId.of(id));

        var ticketDto = composeDto(ticket, zoneId);

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("ticketDetailsEditMode", true);
        model.addAttribute("editDetailsForm", new TicketEditCommandDto(ticket.getVersion(), ticket.getSubject(), ticket.getText()));

        return "ticket/ticket-details :: ticket-details-form";
    }

    @PutMapping("/tickets/{id}")
    public String editTicketDetails(@CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr, 
                                    @PathVariable int id, 
                                    HttpServletResponse response, 
                                    @Validated @ModelAttribute("editDetailsForm") TicketEditCommandDto editDetailsForm, BindingResult bindingResult, Model model) {
        var zoneId = ZoneId.of(zoneIdStr);
        var ticketId = TicketId.of(id);

        if (bindingResult.hasErrors()) {
            // Return just the fragment containing the form and errors
            // we still need ticket id to compose the submit URL 
            var ticketDto = new TicketDto(id);
            model.addAttribute("ticket", ticketDto);
            model.addAttribute("ticketDetailsEditMode", true);
            model.addAttribute("editDetailsForm", editDetailsForm);
            
            response.setHeader("HX-Retarget", "#ticket-details-form"); 
            return "ticket/ticket-details :: ticket-details-form"; 
        }

        var concurrentModificationDetected = false;
        try {
            ticketService.editTicketDitails(editDetailsForm.toCommand(ticketId));
        } catch (OptimisticLockingFailureException | TicketConcurrentModificationException ex) {
           concurrentModificationDetected = true; 
        }

        var ticket = ticketService.getTicket(ticketId);
        var ticketDto = composeDto(ticket, zoneId);

        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();

        if (concurrentModificationDetected) {
            model.addAttribute("ticket", ticketDto);
            model.addAttribute("ticketDetailsEditMode", true);
            model.addAttribute("editDetailsForm", editDetailsForm);

            response.setHeader("HX-Retarget", "#ticket-details-form");

            htmxEventHeaderBuilder.addVersionUpdatedEvent(ticket.getVersion());
            htmxEventHeaderBuilder.addCustomErrorEvent(messageSource.getMessage("ticket.error.concurrent", null, null));

            // NOTE: use 'After-Swap' because current form needs to be updated as well
            response.setHeader("HX-Trigger-After-Swap", htmxEventHeaderBuilder.toJsonString());

            return "ticket/ticket-details :: ticket-details-form"; 
        }

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("ticketDetailsEditMode", false);
        
        htmxEventHeaderBuilder.addVersionUpdatedEvent(ticket.getVersion());
        response.setHeader("HX-Trigger", htmxEventHeaderBuilder.toJsonString());

        return "ticket/ticket-details :: ticket-details-display";
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


    @GetMapping("/tickets/{id}/reply")
    public String getReplyEdit(
        @CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr, 
        Model model, 
        @PathVariable("id") long ticketId,
        @RequestParam("replyId") long replyId) {
        var zoneId = ZoneId.of(zoneIdStr);
        var ticket = ticketService.getTicket(TicketId.of(ticketId));

        var ticketDto = composeDto(ticket, zoneId);
        var ticketReplyId = TicketReplyId.of(replyId);

        var replyDto = ticket.getReplies().stream()
            .filter(r -> r.getId().equals(ticketReplyId))
            .findFirst()
            .map(reply -> new TicketEditReplyCommandDto(ticket.getVersion(), reply.getId().getValue(), reply.getText()))
            .orElseThrow(() -> MissingTicketReplyException.forReplyId(ticketReplyId));

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("replyform", replyDto);

        return "ticket/ticket-details :: reply-form-block(replyFormMode='edit')";
    }

    @PutMapping("/tickets/{id}/reply")
    public String editReply(
        @CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr,
        HttpServletResponse response,
        @PathVariable("id") long id,
        @Validated @ModelAttribute("replyform") TicketEditReplyCommandDto replyform, BindingResult bindingResult, Model model) {
        
        var zoneId = ZoneId.of(zoneIdStr);
        var ticketId = TicketId.of(id);

        if (bindingResult.hasErrors()) {
            // Return just the fragment containing the form and errors
            // we still need ticket id to compose the submit URL 
            model.addAttribute("ticket", new TicketDto(id));
            model.addAttribute("replyform", replyform);
            // Normal render, no OOB
            model.addAttribute("isOobSwap", false);
            
            response.setHeader("HX-Retarget", "#reply-form-section"); 
            return "ticket/ticket-details :: reply-form-block(replyFormMode='edit')"; 
        }

        var concurrentModificationDetected = false;
        try {
            ticketService.editReply(replyform.toCommand(ticketId));
        } catch (OptimisticLockingFailureException | TicketConcurrentModificationException ex) {
           concurrentModificationDetected = true; 
        }

        var ticket = ticketService.getTicket(ticketId);

        var ticketDto = composeDto(ticket, zoneId);

        // Re-populate ticket data needed for the fragment layout
        model.addAttribute("ticket", ticketDto);
        // Force HTMX to swap the form out-of-band
        model.addAttribute("isOobSwap", true); 
        
        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();
        
        if (concurrentModificationDetected) {
            model.addAttribute("replyform", new TicketEditReplyCommandDto(ticket.getVersion(), replyform.getReplyId(), replyform.getText())); // keep the form data with new version
            model.addAttribute("replyFormMode", "edit");
            htmxEventHeaderBuilder.addCustomErrorEvent(messageSource.getMessage("ticket.error.concurrent", null, null));
        } else {
            model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion())); // Reset form
        }

        htmxEventHeaderBuilder.addVersionUpdatedEvent(ticket.getVersion());
        response.setHeader("HX-Trigger", htmxEventHeaderBuilder.toJsonString());
        
        return "ticket/ticket-details :: details-composite-bundle";
    }


    @PostMapping("/tickets/{id}/reply")
    public String replyTicket(@CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr, @PathVariable int id, HttpServletResponse response, @Validated @ModelAttribute("replyform") TicketReplyCommandDto replyform,
                               BindingResult bindingResult, Model model) {
        var zoneId = ZoneId.of(zoneIdStr);
        var ticketId = TicketId.of(id);
    
        if (bindingResult.hasErrors()) {
            // Return just the fragment containing the form and errors
            // we still need ticket id to compose the submit URL 
            var ticketDto = new TicketDto(id);
            model.addAttribute("ticket", ticketDto);
            model.addAttribute("replyform", replyform);
            // Normal render, no OOB
            model.addAttribute("isOobSwap", false);
            
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

        var concurrentModificationDetected = false;
        try {
            ticketService.replyTicket(replyform.toCommand(ticketId));
        } catch (OptimisticLockingFailureException | TicketConcurrentModificationException ex) {
           concurrentModificationDetected = true; 
        }

        var ticket = ticketService.getTicket(ticketId);

        var ticketDto = composeDto(ticket, zoneId);

        // Re-populate ticket data needed for the fragment layout
        model.addAttribute("ticket", ticketDto);
        // Force HTMX to swap the form out-of-band
        model.addAttribute("isOobSwap", true); 
        
        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();

        if (concurrentModificationDetected) {
            htmxEventHeaderBuilder.addCustomErrorEvent(messageSource.getMessage("ticket.error.concurrent", null, null));
            model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion(), replyform.getText())); // keep the form data with new version
        } else {
            model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion())); // Reset form
        }

        htmxEventHeaderBuilder.addVersionUpdatedEvent(ticket.getVersion());
        response.setHeader("HX-Trigger", htmxEventHeaderBuilder.toJsonString());
        
        /**
         * Thymeleaf evaluates the bundle, strings the HTML from both fragments together into a single response body, and passes it to HTMX.
         * HTMX takes the entire string chunk. It drops the reply-section HTML into the main hx-target (#reply-section).
         * It spots hx-swap-oob="true" on the reply-form-section element, plucks it out of the stream, and teleports it to replace 
         * the reply form wherever it sits on the screen.
         */
        return "ticket/ticket-details :: details-composite-bundle";
    }

    @PostMapping("/tickets/{id}/close")
    public String closeTicket(@CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr, 
                              @PathVariable int id,
                              @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                              HttpServletResponse response, 
                              @Validated @ModelAttribute("closeForm") TicketCloseCommandDto closeForm, BindingResult bindingResult, Model model) {
        var zoneId = ZoneId.of(zoneIdStr);
        var ticketId = TicketId.of(id);

        var concurrentModificationDetected = false;
        try {
            ticketService.closeTicket(closeForm.toCommand(ticketId));
        } catch (OptimisticLockingFailureException | TicketConcurrentModificationException ex) {
           concurrentModificationDetected = true; 
        }

        var ticket = ticketService.getTicket(ticketId);
        var ticketDto = composeDto(ticket, zoneId);

        model.addAttribute("ticket", ticketDto);

        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();

        if (concurrentModificationDetected) {
            htmxEventHeaderBuilder.addCustomErrorEvent(messageSource.getMessage("ticket.error.concurrent", null, null));
        }
        model.addAttribute("closeForm", new TicketCloseCommandDto(ticket.getVersion()));
        model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion()));
        
        htmxEventHeaderBuilder.addVersionUpdatedEvent(ticket.getVersion());
        response.setHeader("HX-Trigger-After-Swap", htmxEventHeaderBuilder.toJsonString());

        return "ticket/ticket-details :: page-content-block";
    }

    private TicketDto composeDto(TicketDetailsView ticket, ZoneId zoneId) {
        return new TicketDto(
            ticket.getId().toString(), 
            ticket.getVersion(),
            ticket.getAuthor().getId().toString(), 
            ticket.getStatus().getDisplayName(),
            ticket.getStatus().getCode(),
            ticket.getSubject(),
            ticket.getText(), 
            ticket.getAuthor().getFullName(), 
            ticket.getCreated().atZoneSameInstant(zoneId).toOffsetDateTime(), 
            ticket.getUpdated().atZoneSameInstant(zoneId).toOffsetDateTime(), 
            ticket.getReplies().stream()
                .map(reply -> new TicketReplyDto(
                        reply.getId().toString(), 
                        reply.getAuthor().getId().toString(), 
                        reply.getText(), 
                        reply.getAuthor().getFullName(), 
                        reply.getCreated().atZoneSameInstant(zoneId).toOffsetDateTime(), 
                        reply.getUpdated().atZoneSameInstant(zoneId).toOffsetDateTime()))
                    .toList());
    }
}
