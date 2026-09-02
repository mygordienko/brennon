package com.mygordienko.brennon.ticket.adapter.jdbc;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.ticket.Ticket;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketRequestFilter;
import com.mygordienko.brennon.ticket.TicketStoragePort;

public class JdbcTicketStorage implements TicketStoragePort {

    public JdbcTicketStorage(JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "created", "updated");

    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> getTicketsByAuthor(BrennonUserId userId, TicketRequestFilter filter, Pageable pageable) {
        var safePageable = processPageable(pageable);
        var criteria = Criteria.where("author_id").is(userId);

        if (filter.status() != null) {
            criteria = criteria.and("status").is(filter.status().getCode());
        }

        if (filter.id() != null) {
            criteria = criteria.and("id").is(filter.id().getValue());
        }

        if (filter.subject() != null) {
            criteria = criteria.and("subject").like("%" + filter.subject() + "%").ignoreCase(true);
        }

        var query = Query.query(criteria)
            .limit(safePageable.getPageSize())
            .offset(safePageable.getPageNumber() * safePageable.getPageSize())
            .sort(safePageable.getSort());

        var pageResults = jdbcAggregateTemplate.findAll(query, Ticket.class);

        var queryCount = Query.query(criteria);
        long totalCount = jdbcAggregateTemplate.count(queryCount,Ticket.class);

        return new PageImpl<>(
            pageResults, 
            safePageable, 
            totalCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketById(TicketId ticketId) {
        var query = Query.query(Criteria.where("id").is(ticketId));
        return jdbcAggregateTemplate.findOne(query, Ticket.class);
    }

    @Override
    @Transactional
    public Ticket saveTicket(Ticket ticket) {
        return jdbcAggregateTemplate.save(ticket);
    }
    

    private Pageable processPageable(Pageable pageable) {
        var direction = resolveDiraction(pageable);
        var column = resolveSortColumn(pageable);

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, column));
    }

    private Direction resolveDiraction(Pageable pageable) {
        return pageable.getSort().isSorted()
            ? pageable.getSort().iterator().next().getDirection()
            : Direction.DESC;
    }
    
    private String resolveSortColumn(Pageable pageable) {
        var sort = pageable.getSort();
        if (!sort.isSorted()) {
            return "id";
        }
        var firstOrder = sort.stream().findFirst().orElse(null);
        if (firstOrder == null || firstOrder.getProperty() == null) {
            return "id";
        }
        var column = firstOrder.getProperty();
        if (!ALLOWED_SORT_COLUMNS.contains(column)) {
            throw new IllegalArgumentException("Invalid sort column: " + column);
        }
        return switch(column) {
            case "created" -> "created";
            case "updated" ->  "updated";
            default -> "id";
        };
    }
}
