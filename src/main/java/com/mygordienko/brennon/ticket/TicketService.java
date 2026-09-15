package com.mygordienko.brennon.ticket;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import com.mygordienko.brennon.core.auth.BrennonUserId;
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
    public Page<TicketView> getMyTickets(TicketRequestFilter filter, Pageable pageable) {
        var currentUser = userProfileService.getCurrentUser();
        var tickets = ticketStoragePort.getTicketsByAuthor(currentUser.getId(), filter, pageable);
        var userIdSet = this.extractRelevantUserIds(tickets.getContent());
        var userProfileMap = userProfileService.getUsers(userIdSet).stream()
            .collect(Collectors.toMap(profile -> profile.getId(), profile -> profile, (t, u) -> t));

        var ticketViews = tickets.map(ticket -> {
                var author = userProfileMap.get(ticket.getAuthor().getId());
                var assignee = ticket.getAssignee()
                    .map(ref -> ref.getId())
                    .map(id -> userProfileMap.get(id))
                    .orElse(null);

                return new TicketView(
                    ticket.getId(),
                    ticket.getVersion(),
                    ticket.getStatus(),
                    author,
                    assignee,
                    ticket.getSubject(),
                    ticket.getText(),
                    ticket.getCreated(),
                    ticket.getUpdated()
                );
            });

        return ticketViews;
    }

    @PreAuthorize("hasAuthority('support:read')")
    @Transactional(readOnly = true)
    public Page<TicketView> getUnassignedTickets(TicketRequestFilter filter, Pageable pageable) {
        var tickets = ticketStoragePort.getTicketsByAssignee(null, filter, pageable);
        var userIdSet = this.extractRelevantUserIds(tickets.getContent());
        var userProfileMap = userProfileService.getUsers(userIdSet).stream()
            .collect(Collectors.toMap(profile -> profile.getId(), profile -> profile, (t, u) -> t));

        var ticketViews = tickets.map(ticket -> {
                var author = userProfileMap.get(ticket.getAuthor().getId());
                var assignee = ticket.getAssignee()
                    .map(ref -> ref.getId())
                    .map(id -> userProfileMap.get(id))
                    .orElse(null);

                return new TicketView(
                    ticket.getId(),
                    ticket.getVersion(),
                    ticket.getStatus(),
                    author,
                    assignee,
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
            .filter(t -> ticketSecurityManager.userCanRead(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(ticketId));

        var replyAuthorsIds = ticket.getReplies().stream()
            .map(reply -> reply.getAuthor().getId())
            .toList();

        var assignee = ticket.getAssignee()
            .map(ref -> ref.getId())
            .map(id -> userProfileService.getUser(id))
            .orElse(null);

        var author = userProfileService.getUser(ticket.getAuthor().getId());

        var userProfileMap = Stream.concat(
                userProfileService.getUsers(replyAuthorsIds).stream(), 
                Stream.of(author, assignee)
            )
            .filter(Objects::nonNull)
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
                    reply.getUpdated(),
                    new TicketReplyView.UserRelation(currentUser.getId().equals(authorProfile.getId()))
                );
            })
            .toList();

        return new TicketDetailsView(
                ticket.getId(),
                ticket.getVersion(),
                ticket.getStatus(),
                userProfileMap.get(ticket.getAuthor().getId()),
                assignee,
                ticket.getSubject(),
                ticket.getText(),
                ticket.getCreated(),
                ticket.getUpdated(),
                new TicketDetailsView.UserRelation(
                    currentUser.getId().equals(ticket.getAuthor().getId()),
                    currentUser.getId().equals(ticket.getAuthor().getId()),
                    currentUser.getId().equals(ticket.getAuthor().getId()) || currentUser.getId().equals(ticket.getAssignee().map(ref -> ref.getId()).orElse(null)),
                    !currentUser.getId().equals(ticket.getAssignee().map(ref -> ref.getId()).orElse(null))
                ),
                replyViews
            );
    }

    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket createTicket(TicketCreateCommand command) {
        var created = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());
        var currentUser = userProfileService.getCurrentUser();
        
        var ticket = Ticket.createNew(TicketStatus.NEW, currentUser.getId(), null, command.subject(), command.text(), created, created);

        return ticketStoragePort.saveTicket(ticket);
    }


    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket replyTicket(TicketReplyCommand command) {
        var currentUser = userProfileService.getCurrentUser();
        var created = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());

        var ticket = ticketStoragePort.getTicketById(command.ticketId())
            // TODO: use AOP
            .filter(t -> ticketSecurityManager.userCanReply(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(command.ticketId()));

        if (ticket.getVersion() != command.version()) {
            throw TicketConcurrentModificationException.forTicket(command.ticketId());
        }

        ticket.addReply(currentUser.getId(), command.text(), created);

        return ticketStoragePort.saveTicket(ticket);
    }


    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket editTicketDitails(TicketEditCommand command) {
        var currentUser = userProfileService.getCurrentUser();
        var updated = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());

        var ticket = ticketStoragePort.getTicketById(command.ticketId())
            // TODO: use AOP
            .filter(t -> ticketSecurityManager.userCanEdit(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(command.ticketId()));

        if (ticket.getVersion() != command.version()) {
            throw TicketConcurrentModificationException.forTicket(command.ticketId());
        }

        ticket.editDetails(currentUser.getId(), command.subject(), command.text(), updated);

        return ticketStoragePort.saveTicket(ticket);
    }

    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket editReply(TicketEditReplyCommand command) {
        var currentUser = userProfileService.getCurrentUser();
        var created = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());

        var ticket = ticketStoragePort.getTicketById(command.ticketId())
            // TODO: use AOP
            .filter(t -> ticketSecurityManager.userCanReply(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(command.ticketId()));

        if (ticket.getVersion() != command.version()) {
            throw TicketConcurrentModificationException.forTicket(command.ticketId());
        }

        ticket.editReply(currentUser.getId(), command.replyId(), command.text(), created);
        
        return ticketStoragePort.saveTicket(ticket);
    }

    @PreAuthorize("hasAuthority('user:write')")
    @Transactional
    public Ticket closeTicket(TicketCloseCommand command) {
        var currentUser = userProfileService.getCurrentUser();
        var updated = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());

        var ticket = ticketStoragePort.getTicketById(command.ticketId())
            // TODO: use AOP
            .filter(t -> ticketSecurityManager.userCanClose(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(command.ticketId()));

        if (ticket.getVersion() != command.version()) {
            throw TicketConcurrentModificationException.forTicket(command.ticketId());
        }

        ticket.closeTicket(currentUser.getId(), updated);

        return ticketStoragePort.saveTicket(ticket);
    }

    @PreAuthorize("hasAuthority('support:write')")
    @Transactional
    public Ticket assignMeTicket(TicketAssignMeCommand command) {
        var currentUser = userProfileService.getCurrentUser();
        var updated = OffsetDateTime.ofInstant(clockManager.now(), ZoneId.systemDefault());

        var ticket = ticketStoragePort.getTicketById(command.ticketId())
            // TODO: use AOP
            .filter(t -> ticketSecurityManager.userCanAssign(currentUser, t))
            .orElseThrow(() -> MissingTicketException.forTicket(command.ticketId()));

        if (ticket.getVersion() != command.version()) {
            throw TicketConcurrentModificationException.forTicket(command.ticketId());
        }

        ticket.assignTo(currentUser.getId(), updated);

        return ticketStoragePort.saveTicket(ticket);
    }

    private Set<BrennonUserId> extractRelevantUserIds(Collection<Ticket> tickets) {
        var authorsIds = tickets.stream()
            .map(t -> t.getAuthor().getId())
            .toList();

        var assigneeIds = tickets.stream()
            .map(t -> t.getAssignee())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(u -> u.getId())
            .toList();

        var userIdSet = new HashSet<BrennonUserId>();
        userIdSet.addAll(authorsIds);
        userIdSet.addAll(assigneeIds);

        return userIdSet;
    }
}
