package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;
import com.mygordienko.brennon.core.common.ClockManager;

public class TicketService {
    public TicketService(
        BrennonUserProfileService userProfileService, 
        TicketStoragePort ticketStoragePort,
        TicketSecurityManager ticketSecurityManager,
        ClockManager clockManager) {
        this.userProfileService = userProfileService;
        this.ticketStoragePort = ticketStoragePort;
        this.ticketSecurityManager = ticketSecurityManager;
        this.clockManager = clockManager;
    }

    private final BrennonUserProfileService userProfileService;
    private final TicketStoragePort ticketStoragePort;
    private final TicketSecurityManager ticketSecurityManager;
    private final ClockManager clockManager;

    @PreAuthorize("hasAuthority('user:read')")
    @Transactional(readOnly = true)
    public Page<TicketView> getMyTickets(Pageable pageable) {
        var currentUser = userProfileService.getCurrentUser();

        var tickets = ticketStoragePort.getTicketsByAuthor(currentUser.getId(), pageable);

        var ticketViews = tickets.map(ticket -> {
                return new TicketView(
                    ticket.getId(),
                    ticket.getVersion(),
                    ticket.getStatus(),
                    currentUser,
                    ticket.getSubject(),
                    ticket.getText(),
                    ticket.getCreated(),
                    ticket.getUpdated()
                );
            });

        return ticketViews;
    }

    @PreAuthorize("hasAuthority('user:read')")
    @Transactional(readOnly = true)
    public TicketDetailsView getTicket(TicketId ticketId) {
        var currentUser = userProfileService.getCurrentUser();

        var ticket = ticketStoragePort.getTicketById(ticketId)
            // TODO: use AOP
            .filter(t -> ticketSecurityManager.userHasAccess(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(ticketId));

        var replyAuthorsIds = ticket.getReplies().stream()
            .map(reply -> reply.getAuthor().getId())
            .toList();

        var userProfileMap = Stream.concat(
                userProfileService.getUsers(replyAuthorsIds).stream(), 
                Stream.of(userProfileService.getUser(ticket.getAuthor().getId())))
            .collect(Collectors.toMap(profile -> profile.getId(), profile -> profile, (t, u) -> t));

        var replyViews = ticket.getReplies().stream()
            .map(reply -> {
                var authorProfile = userProfileMap.get(reply.getAuthor().getId());
                return new TicketReplyView(
                    reply.getId(), 
                    reply.getTicketId(), 
                    authorProfile, 
                    reply.getText(),
                    reply.getCreated(),
                    reply.getUpdated()
                );
            })
            .toList();

        return new TicketDetailsView(
                ticket.getId(),
                ticket.getVersion(),
                ticket.getStatus(),
                userProfileMap.get(ticket.getAuthor().getId()),
                ticket.getSubject(),
                ticket.getText(),
                ticket.getCreated(),
                ticket.getUpdated(),
                replyViews
            );
    }

    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket createTicket(TicketCreateCommand command) {
        var created = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());
        var currentUser = userProfileService.getCurrentUser();
        
        var ticket = Ticket.createNew(TicketStatus.NEW, currentUser.getId(), command.subject(), command.text(), created, created);

        return ticketStoragePort.saveTicket(ticket);
    }


    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket replyTicket(TicketReplyCommand command) {
        var currentUser = userProfileService.getCurrentUser();
        var created = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());

        var ticket = ticketStoragePort.getTicketById(command.ticketId())
            // TODO: use AOP
            .filter(t -> ticketSecurityManager.userHasAccess(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(command.ticketId()));

        if (ticket.getVersion() != command.version()) {
            throw TicketConcurrentModificationException.forTicket(command.ticketId());
        }

        ticket.addReply(currentUser.getId(), command.text(), created);

        return ticketStoragePort.saveTicket(ticket);
    }
}
