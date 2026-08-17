package com.mygordienko.brennon.ticket.adapter.jdbc;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * TODO: delete when ticket creation is implemented
 */
@Component
public class TestDataPopulator {
    
    public TestDataPopulator(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private final JdbcClient jdbcClient;

    @EventListener
    public void populate(ApplicationReadyEvent event) {
        // clear database
        jdbcClient
            .sql(
                """
                DELETE FROM ticket_replies;
                """)
                .update();
        
        jdbcClient
            .sql(
                """
                DELETE FROM tickets;
                """)
                .update();
        

        // init database
        jdbcClient
            .sql(
                """
                INSERT INTO tickets(id, author_id, subject, text, status, created, updated)
                VALUES 
                    ('1', '1', 'Power outage in office 1', 'No power, please fix asap', 10, '2026-08-15 18:00:00+03', '2026-08-15 18:00:10+03'),
                    ('2', '1', 'Cant login', 'HR desk does not accept my creds', 30, '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '1', 'Slow internet', 'Cant attend live meetings', 20, '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '1', 'AC is not working', 'AC is not working', 20, '2026-08-15 14:10:00+03', '2026-08-15 14:26:00+03'),
                    ('5', '2', 'Fridge is broken', 'Fridge is broken', 10, '2026-08-15 10:10:00+03', '2026-08-15 11:46:00+03');
                """)
                .update();

        jdbcClient
            .sql(
                """
                INSERT INTO ticket_replies(id, author_id, ticket_id, text, created, updated)
                VALUES 
                    ('1', '2', '1', 'reply 1 from 2 on ticket 1', '2026-08-15 18:00:05+03', '2026-08-15 18:00:10+03'),
                    ('2', '2', '1', 'reply 2 from 2 on ticket 1', '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '2', '2', 'reply 1 from 2 on ticket 2', '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '2', '2', 'reply 1 from 2 on ticket 2', '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """)
                .update();
    }
}
