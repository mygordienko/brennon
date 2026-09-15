package com.mygordienko.brennon.ticket.adapter.web;

import java.time.ZoneId;
import java.util.Arrays;

import org.springframework.data.domain.Page;
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
import com.mygordienko.brennon.ticket.TicketCreateCommand;
import com.mygordienko.brennon.ticket.TicketDetailsView;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;
import com.mygordienko.brennon.ticket.TicketService;
import com.mygordienko.brennon.ticket.TicketStatus;
import com.mygordienko.brennon.ticket.TicketView;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class TicketController {
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    private final TicketService ticketService;

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
        .map(ticket -> composeDto(ticket, zoneId))
        .toList();

        model.addAttribute("tickets", ticketsDtos);
        model.addAttribute("ticketRequestFilter", filterDto);
        model.addAttribute("statuses", statuses);
        model.addAttribute("page", composeDto(page));

        if (hxRequest != null) {
            // this is a HTMX request, serve only fragment
            return "ticket/ticket-list :: page-content-block";
        } else {
            // regular request, serve full template
            return "ticket/ticket-list";
        }
    }

    @GetMapping("/tickets/unassigned")
    public String getunassignedTickets(
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

        var page = ticketService.getUnassignedTickets(filter, pageable);
        var zoneId = ZoneId.of(zoneIdStr);

        var ticketsDtos = page.getContent().stream()
        .map(ticket -> composeDto(ticket, zoneId))
        .toList();

        model.addAttribute("tickets", ticketsDtos);
        model.addAttribute("ticketRequestFilter", filterDto);
        model.addAttribute("statuses", statuses);
        model.addAttribute("page", composeDto(page));

        if (hxRequest != null) {
            // this is a HTMX request, serve only fragment
            return "ticket/support-unassigned-ticket-list :: page-content-block";
        } else {
            // regular request, serve full template
            return "ticket/support-unassigned-ticket-list";
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
        model.addAttribute("assignMeForm", new TicketAssignMeCommandDto(ticket.getVersion()));

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
            var ticket = ticketService.getTicket(ticketId);
            var ticketDto = composeDto(ticket, zoneId);
            model.addAttribute("ticket", ticketDto);
            model.addAttribute("ticketDetailsEditMode", true);
            model.addAttribute("editDetailsForm", editDetailsForm);
            
            response.setHeader("HX-Retarget", "#ticket-details-form"); 
            return "ticket/ticket-details :: ticket-details-form"; 
        }

        ticketService.editTicketDitails(editDetailsForm.toCommand(ticketId));

        var ticket = ticketService.getTicket(ticketId);
        var ticketDto = composeDto(ticket, zoneId);

        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();

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
            var ticket = ticketService.getTicket(ticketId);
            var ticketDto = composeDto(ticket, zoneId);

            model.addAttribute("ticket", ticketDto);
            model.addAttribute("replyform", replyform);
            // Normal render, no OOB
            model.addAttribute("isOobSwap", false);
            
            response.setHeader("HX-Retarget", "#reply-form-section"); 
            return "ticket/ticket-details :: reply-form-block(replyFormMode='edit')"; 
        }

        ticketService.editReply(replyform.toCommand(ticketId));

        var ticket = ticketService.getTicket(ticketId);

        var ticketDto = composeDto(ticket, zoneId);

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("isOobSwap", true);         
        model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion())); // Reset form
        
        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();
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
            var ticket = ticketService.getTicket(ticketId);
            var ticketDto = composeDto(ticket, zoneId);

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

        ticketService.replyTicket(replyform.toCommand(ticketId));

        var ticket = ticketService.getTicket(ticketId);
        var ticketDto = composeDto(ticket, zoneId);

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("isOobSwap", true); 
        model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion())); // Reset form
        
        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();
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

        ticketService.closeTicket(closeForm.toCommand(ticketId));

        var ticket = ticketService.getTicket(ticketId);
        var ticketDto = composeDto(ticket, zoneId);

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("closeForm", new TicketCloseCommandDto(ticket.getVersion()));
        model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion()));
        model.addAttribute("assignMeForm", new TicketAssignMeCommandDto(ticket.getVersion()));
        
        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();
        htmxEventHeaderBuilder.addVersionUpdatedEvent(ticket.getVersion());
        response.setHeader("HX-Trigger-After-Swap", htmxEventHeaderBuilder.toJsonString());

        return "ticket/ticket-details :: page-content-block";
    }

    @PostMapping("/tickets/{id}/assignme")
    public String assignMeTicket(@CookieValue(value = "BRENNON_CLIENT_TIMEZONE", defaultValue = "UTC") String zoneIdStr, 
                              @PathVariable int id,
                              @RequestHeader(value = "HX-Request", required = false) String hxRequest,
                              HttpServletResponse response, 
                              @Validated @ModelAttribute("assignMeForm") TicketAssignMeCommandDto assignMeForm, BindingResult bindingResult, Model model) {
        var zoneId = ZoneId.of(zoneIdStr);
        var ticketId = TicketId.of(id);

        ticketService.assignMeTicket(assignMeForm.toCommand(ticketId));

        var ticket = ticketService.getTicket(ticketId);
        var ticketDto = composeDto(ticket, zoneId);

        model.addAttribute("ticket", ticketDto);
        model.addAttribute("assignMeForm", new TicketAssignMeCommandDto(ticket.getVersion()));
        model.addAttribute("closeForm", new TicketCloseCommandDto(ticket.getVersion()));
        model.addAttribute("replyform", new TicketReplyCommandDto(ticket.getVersion()));
        
        var htmxEventHeaderBuilder = new HtmxClientEventTriggerHeaderBuilder();
        htmxEventHeaderBuilder.addVersionUpdatedEvent(ticket.getVersion());
        response.setHeader("HX-Trigger-After-Swap", htmxEventHeaderBuilder.toJsonString());

        return "ticket/ticket-details :: page-content-block";
    }

    private TicketDto composeDto(TicketDetailsView ticket, ZoneId zoneId) {

        var authorInfo = new TicketDto.UserInfoDto(ticket.getAuthor().getId().toString(), ticket.getAuthor().getFullName());

        var assigneeInfo = ticket.getAssignee()
            .map(u -> new TicketDto.UserInfoDto(u.getId().toString(), u.getFullName()))
            .orElse(new TicketDto.UserInfoDto("", ""));

        return new TicketDto(
            ticket.getId().toString(), 
            ticket.getVersion(),
            authorInfo,
            assigneeInfo,
            ticket.getStatus().getDisplayName(),
            ticket.getStatus().getCode(),
            ticket.getSubject(),
            ticket.getText(), 
            ticket.getCreated().atZoneSameInstant(zoneId).toOffsetDateTime(), 
            ticket.getUpdated().atZoneSameInstant(zoneId).toOffsetDateTime(), 
            new TicketDto.UserRelationDto(
                ticket.getUserRelation().canClose(),
                ticket.getUserRelation().canEdit(),
                ticket.getUserRelation().canReply(),
                ticket.getUserRelation().canAssign()
            ),
            ticket.getReplies().stream()
                .map(reply -> new TicketReplyDto(
                        reply.getId().toString(), 
                        reply.getAuthor().getId().toString(), 
                        reply.getText(), 
                        reply.getAuthor().getFullName(), 
                        reply.getCreated().atZoneSameInstant(zoneId).toOffsetDateTime(), 
                        reply.getUpdated().atZoneSameInstant(zoneId).toOffsetDateTime(),
                        new TicketReplyDto.UserRelationDto(
                            reply.getUserRelation().isAuthor())
                        )
                    )
                    .toList());
    }

    private TicketRecordDto composeDto(TicketView ticket, ZoneId zoneId) {
        return new TicketRecordDto(
            ticket.getId().toString(),
            ticket.getVersion(),
            ticket.getStatus().getDisplayName(),
            ticket.getSubject(),
            ticket.getAuthor().getFullName(),
            ticket.getCreated().atZoneSameInstant(zoneId).toOffsetDateTime(),
            ticket.getUpdated().atZoneSameInstant(zoneId).toOffsetDateTime()
        );
    }

    private PageInfoDto composeDto(Page page) {
         var order = page.getSort().stream()
            .findAny()
            .orElseThrow();

        var orderDto = new PageInfoDto.OrderDto(order.getProperty(), order.getDirection().name());

        return new PageInfoDto(page.getNumber(), page.getNumberOfElements(), page.getTotalPages(), page.getSize(), orderDto);
    }
}
