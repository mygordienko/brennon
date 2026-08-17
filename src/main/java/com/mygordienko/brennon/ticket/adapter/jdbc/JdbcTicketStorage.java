package com.mygordienko.brennon.ticket.adapter.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.BrennonUserProfile;
import com.mygordienko.brennon.ticket.Ticket;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReply;
import com.mygordienko.brennon.ticket.TicketReplyId;
import com.mygordienko.brennon.ticket.TicketStatus;
import com.mygordienko.brennon.ticket.TicketStoragePort;

public class JdbcTicketStorage implements TicketStoragePort {
    public JdbcTicketStorage(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcClient = JdbcClient.create(jdbcTemplate.getJdbcTemplate());
    }

    private final JdbcClient jdbcClient;
    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "created", "updated");

    @Override
    @Transactional(readOnly = true)
    public Page<Ticket> getTicketsByAuthor(BrennonUserId authorId, Pageable pageable) {

        var totalElements = getTotalCount(authorId);

        var targetTicketIds = selectRelevantTickets(authorId, pageable).stream()
            .map(ticketId -> ticketId.toString())
            .toList();

        var sql = """
            SELECT t.id, t.subject, t.text, t.status, t.author_id AS ticket_author_id,
                   t.created AS ticket_created, t.updated AS ticket_updated,
                   tp1.username AS ticket_author_username, tp1.first_name AS ticket_author_first_name,
                   tp1.last_name AS ticket_author_last_name,
                   tr.id AS reply_id, tr.ticket_id AS reply_ticket_id, tr.text AS reply_text,
                   tr.author_id AS reply_author_id, tr.created AS reply_created, tr.updated AS reply_updated,
                   tp2.username AS reply_author_username, tp2.first_name AS reply_author_first_name,
                   tp2.last_name AS reply_author_last_name
            FROM tickets t
            LEFT JOIN ticket_replies tr ON tr.ticket_id = t.id
            LEFT JOIN user_profiles tp1 ON t.author_id = tp1.id
            LEFT JOIN user_profiles tp2 ON tr.author_id = tp2.id
            WHERE t.id = ANY(:ids)
            ORDER BY array_position(:ids, t.id)
            """;

        var paramMap = java.util.Map.of("ids", targetTicketIds.toArray(new String[0]));

        var tickets = jdbcClient.sql(sql)
            .params(paramMap)
            .query(new FullTicketExctractor());

        return new PageImpl<>(tickets, pageable, totalElements);
    }

    private List<TicketId> selectRelevantTickets(BrennonUserId authorId, Pageable pageable) {
        var sortColumn = resolveSortColumn(pageable);
        var sortDirection = pageable.getSort().isSorted()
            ? pageable.getSort().iterator().next().getDirection().name()
            : "DESC";
        var limit = pageable.getPageSize();
        var offset = (int) pageable.getOffset();

        var sql = """
            SELECT t.id AS ticket_id, t.subject, t.text, t.status, t.author_id AS ticket_author_id,
                   t.created AS ticket_created, t.updated AS ticket_updated
            FROM tickets t
            WHERE t.author_id = :authorId
            ORDER BY %s %s
            LIMIT :limit OFFSET :offset
            """.formatted(sortColumn, sortDirection);

        var paramMap = java.util.Map.of(
            "authorId", authorId.toString(),
            "limit", limit,
            "offset", offset);

        return jdbcClient.sql(sql)
            .params(paramMap)
            .query((ResultSetExtractor<List<TicketId>>) rs -> {
                var result = new ArrayList<TicketId>();
                while (rs.next()) {
                    var ticketId = TicketId.of(rs.getString("ticket_id"));
                    result.add(ticketId);
                }

                return result;
            });
    }

    private Long getTotalCount(BrennonUserId authorId) {
        var paramMap = java.util.Map.of("authorId", authorId.toString());

        var sql = """
            SELECT COUNT(*)
            FROM tickets t
            WHERE t.author_id = :authorId
            """;

        return jdbcClient.sql(sql)
            .params(paramMap)
            .query(Long.class)
            .single();
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
            case "created" -> "ticket_created";
            case "updated" ->  "ticket_updated";
            default -> "id";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketById(TicketId ticketId) {
        var sql = """
            SELECT t.id, t.subject, t.text, t.status, t.author_id AS ticket_author_id,
                   t.created AS ticket_created, t.updated AS ticket_updated,
                   tp1.username AS ticket_author_username, tp1.first_name AS ticket_author_first_name,
                   tp1.last_name AS ticket_author_last_name,
                   tr.id AS reply_id, tr.ticket_id AS reply_ticket_id, tr.text AS reply_text,
                   tr.author_id AS reply_author_id, tr.created AS reply_created, tr.updated AS reply_updated,
                   tp2.username AS reply_author_username, tp2.first_name AS reply_author_first_name,
                   tp2.last_name AS reply_author_last_name
            FROM tickets t
            LEFT JOIN ticket_replies tr ON tr.ticket_id = t.id
            LEFT JOIN user_profiles tp1 ON t.author_id = tp1.id
            LEFT JOIN user_profiles tp2 ON tr.author_id = tp2.id
            WHERE t.id = :ticketId
            """;

        var paramMap = Map.of("ticketId", ticketId.toString());

        return jdbcClient
            .sql(sql)
            .params(paramMap)
            .query(new FullTicketExctractor())
            .stream()
            .findAny();
    }


    private static class FullTicketExctractor implements ResultSetExtractor<List<Ticket>> {
        @Override
        public List<Ticket> extractData(ResultSet rs) throws SQLException, DataAccessException {
            var result = new ArrayList<Ticket>();
            var ticketsLoaded = new HashMap<TicketId, Ticket>();
            var ticketReplies = new HashMap<TicketId, List<TicketReply>>();

            while (rs.next()) {
                var ticketId = TicketId.of(rs.getString("id"));
                if (!ticketsLoaded.containsKey(ticketId)) {
                    var author = new BrennonUserProfile(
                        BrennonUserId.of(rs.getString("ticket_author_id")),
                        BrennonUserLogin.of(rs.getString("ticket_author_username")),
                        rs.getString("ticket_author_first_name"),
                        rs.getString("ticket_author_last_name"));

                    Ticket ticket = new Ticket(
                        ticketId,
                        TicketStatus.of(rs.getInt("status")),
                        author,
                        rs.getString("subject"),
                        rs.getString("text"),
                        OffsetDateTime.ofInstant(rs.getTimestamp("ticket_created").toInstant(), java.time.ZoneOffset.UTC),
                        OffsetDateTime.ofInstant(rs.getTimestamp("ticket_updated").toInstant(), java.time.ZoneOffset.UTC),
                        new ArrayList<>());
                    result.add(ticket);
                    ticketsLoaded.put(ticketId, ticket);
                }
   
                var replyIdString = rs.getString("reply_id");

                if (replyIdString == null) {
                    continue;
                }
                
                var author = new BrennonUserProfile(
                    BrennonUserId.of(rs.getString("reply_author_id")),
                    BrennonUserLogin.of(rs.getString("reply_author_username")),
                    rs.getString("reply_author_first_name"),
                    rs.getString("reply_author_last_name"));

                var reply = new TicketReply(
                    TicketReplyId.of(rs.getString("reply_id")),
                    ticketId,
                    author,
                    rs.getString("reply_text"),
                    OffsetDateTime.ofInstant(rs.getTimestamp("reply_created").toInstant(), java.time.ZoneOffset.UTC),
                    OffsetDateTime.ofInstant(rs.getTimestamp("reply_updated").toInstant(), java.time.ZoneOffset.UTC));


                ticketReplies.computeIfAbsent(ticketId, i -> new ArrayList<>());
                ticketReplies.get(ticketId).add(reply);
            }

            // Tickets are immutable, so we have to re-map to add comments
            return result.stream()
            .map(ticket -> {
                return new Ticket(
                        ticket.getId(),
                        ticket.getStatus(),
                        ticket.getAuthor(),
                        ticket.getSubject(),
                        ticket.getText(),
                        ticket.getCreated(),
                        ticket.getUpdated(),
                        Optional.ofNullable(ticketReplies.get(ticket.getId())).orElse(List.of()));
            })
            .toList();
        }

    }
}
