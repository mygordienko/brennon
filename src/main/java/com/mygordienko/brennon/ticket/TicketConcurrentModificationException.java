package com.mygordienko.brennon.ticket;

import org.springframework.dao.OptimisticLockingFailureException;

/**
 * This exception is used in addition to {@link OptimisticLockingFailureException}.
 * 
 * The reason we need additional exception to represent optimistic lock conflict is HTMX.
 * In a normal(base) scenario, an HTML form represents an aggragate, so the whole aggregate state
 * is sent back to backend on each submit.
 * 
 * With HTMX we can send async requests and have smaller `<form>` that a backed by specific parts of the aggregate.
 * Ex: reply form on ticket details page. While `Ticket` is still an aggregate, pressing "Add reply" button 
 *  does not send the whole ticket state to the backend.
 * 
 * Since we do not send the whole state, we have to re-construct it (aggregate is still needed to check invariants) 
 * by retrieving the aggregate from the database, and the aggreagate will be retrieved with the latest version.
 * This is where we have to do version check and throw an exception (in our case).
 * 
 * Database-level check is still needed and will be done by Spring, potentially throwing `OptimisticLockingFailureException`.
 * 
 */
public class TicketConcurrentModificationException extends RuntimeException {
    private TicketConcurrentModificationException(String message) {
        super(message);
    }

    private static final String MESSAGE_TEMPLATE = "Concurrent update detected for ticket %s";

    public static TicketConcurrentModificationException forTicket(TicketId ticketId) {
        return new TicketConcurrentModificationException(MESSAGE_TEMPLATE.formatted(ticketId));
    }
}
