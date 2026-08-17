package com.mygordienko.brennon.ticket.adapter;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;
import com.mygordienko.brennon.core.auth.di.LiquibaseConfiguration;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;
import com.mygordienko.brennon.ticket.adapter.jdbc.JdbcTicketStorage;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(LiquibaseConfiguration.class)
public class JdbcTicketStorageTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @AfterEach
    public void clean() {
        jdbcTemplate.update("""
                DELETE FROM ticket_replies;
                """, Map.of());

        jdbcTemplate.update("""
                DELETE FROM tickets;
                """, Map.of());
    }

    @Test
    @Transactional
    @DisplayName("Test default sorting on single page result")
    public void givenDataExistsWhenSinglePageAndNoSortingUseDefaultSorting() {
        final var parameterSource = new MapSqlParameterSource();

        // init database
        jdbcTemplate.update(
                """
                INSERT INTO tickets(id, author_id, subject, text, status, created, updated)
                VALUES 
                    ('1', '1', 'Power outage in office 1', 'No power, please fix asap', 10, '2026-08-15 18:00:00+03', '2026-08-15 18:00:10+03'),
                    ('2', '1', 'Cant login', 'HR desk does not accept my creds', 30, '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '1', 'Slow internet', 'Cant attend live meetings', 20, '2026-08-15 10:30:00+03', '2026-08-15 13:00:00+03'),
                    ('4', '2', 'Printer problem', 'Printer got broken', 20, '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """, parameterSource);

        var storage = new JdbcTicketStorage(jdbcTemplate);
        var pageable = PageRequest.of(0, 3);
        
        var results = storage.getTicketsByAuthor(BrennonUserId.of("1"), pageable);

        Assertions.assertThat(results).hasSize(3);
        Assertions.assertThat(results.getContent().get(0).getId()).isEqualTo(TicketId.of("3"));
        Assertions.assertThat(results.getContent().get(1).getId()).isEqualTo(TicketId.of("2"));
        Assertions.assertThat(results.getContent().get(2).getId()).isEqualTo(TicketId.of("1"));
    }


    @Test
    @Transactional
    @DisplayName("Test custom sorting on single page result")
    public void givenDataExistsWhenSinglePageAndCustomSortingUseCustomSorting() {
        final var parameterSource = new MapSqlParameterSource();

        // init database
        jdbcTemplate.update(
                """
                INSERT INTO tickets(id, author_id, subject, text, status, created, updated)
                VALUES 
                    ('1', '1', 'Power outage in office 1', 'No power, please fix asap', 10, '2026-08-15 18:00:00+03', '2026-08-15 18:00:10+03'),
                    ('2', '1', 'Cant login', 'HR desk does not accept my creds', 30, '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '1', 'Slow internet', 'Cant attend live meetings', 20, '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '2', 'Printer problem', 'Printer got broken', 20, '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """, parameterSource);

        var storage = new JdbcTicketStorage(jdbcTemplate);
        var pageable = PageRequest.of(0,3, Direction.ASC, "created");
        
        var results = storage.getTicketsByAuthor(BrennonUserId.of("1"), pageable);

        Assertions.assertThat(results).hasSize(3);
        Assertions.assertThat(results.getContent().get(0).getId()).isEqualTo(TicketId.of("2"));
        Assertions.assertThat(results.getContent().get(1).getId()).isEqualTo(TicketId.of("3"));
        Assertions.assertThat(results.getContent().get(2).getId()).isEqualTo(TicketId.of("1"));
    }

    @Test
    @Transactional
    @DisplayName("Test custom sorting on multi page result")
    public void givenDataExistsWhenMultiPageAndCustomSortingUseCustomSorting() {
        final var parameterSource = new MapSqlParameterSource();

        // init database
        jdbcTemplate.update(
                """
                INSERT INTO tickets(id, author_id, subject, text, status, created, updated)
                VALUES 
                    ('1', '1', 'Power outage in office 1', 'No power, please fix asap', 10, '2026-08-15 18:00:00+03', '2026-08-15 18:00:10+03'),
                    ('2', '1', 'Cant login', 'HR desk does not accept my creds', 30, '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '1', 'Slow internet', 'Cant attend live meetings', 20, '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '2', 'Printer problem', 'Printer got broken', 20, '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """, parameterSource);

        var storage = new JdbcTicketStorage(jdbcTemplate);
        
        var page1 = storage.getTicketsByAuthor(BrennonUserId.of("1"), PageRequest.of(0,1, Direction.ASC, "updated"));
        var page2 = storage.getTicketsByAuthor(BrennonUserId.of("1"), PageRequest.of(1,1, Direction.ASC, "updated"));
        var page3 = storage.getTicketsByAuthor(BrennonUserId.of("1"), PageRequest.of(2,1, Direction.ASC, "updated"));

        Assertions.assertThat(page1).hasSize(1);
        Assertions.assertThat(page1.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(page1.getContent().get(0).getId()).isEqualTo(TicketId.of("2"));

        Assertions.assertThat(page2).hasSize(1);
        Assertions.assertThat(page2.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(page2.getContent().get(0).getId()).isEqualTo(TicketId.of("3"));

        Assertions.assertThat(page3).hasSize(1);
        Assertions.assertThat(page3.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(page3.getContent().get(0).getId()).isEqualTo(TicketId.of("1"));
        
    }


    @Test
    @Transactional
    @DisplayName("Ticket aggregates assembled correctly with paging")
    public void givenDataExistsWhenMultiPageThenTicketsCorrectlyAssembledWithReplies() {
        final var parameterSource = new MapSqlParameterSource();

        // init database
        jdbcTemplate.update(
                """
                INSERT INTO tickets(id, author_id, subject, text, status, created, updated)
                VALUES 
                    ('1', '1', 'Power outage in office 1', 'No power, please fix asap', 10, '2026-08-15 18:00:00+03', '2026-08-15 18:00:10+03'),
                    ('2', '1', 'Cant login', 'HR desk does not accept my creds', 30, '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '1', 'Slow internet', 'Cant attend live meetings', 20, '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '2', 'Printer problem', 'Printer got broken', 20, '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """, parameterSource);

        jdbcTemplate.update(
                """
                INSERT INTO ticket_replies(id, author_id, ticket_id, text, created, updated)
                VALUES 
                    ('1', '2', '1', 'reply 1 from 2 on ticket 1', '2026-08-15 18:00:05+03', '2026-08-15 18:00:10+03'),
                    ('2', '2', '1', 'reply 2 from 2 on ticket 1', '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '2', '2', 'reply 1 from 2 on ticket 2', '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '2', '2', 'reply 1 from 2 on ticket 2', '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """, parameterSource);

        var storage = new JdbcTicketStorage(jdbcTemplate);
        
        var page = storage.getTicketsByAuthor(BrennonUserId.of("1"), PageRequest.of(2,1));

        Assertions.assertThat(page).hasSize(1);
        Assertions.assertThat(page.getTotalElements()).isEqualTo(3);
        Assertions.assertThat(page.getContent().get(0).getId()).isEqualTo(TicketId.of("1"));
        Assertions.assertThat(page.getContent().get(0).getReplies()).hasSize(2);
        Assertions.assertThat(page.getContent().get(0).getReplies().get(0).getId()).isEqualTo(TicketReplyId.of("2"));
        Assertions.assertThat(page.getContent().get(0).getReplies().get(0).getText()).isEqualTo("reply 2 from 2 on ticket 1");
        Assertions.assertThat(page.getContent().get(0).getReplies().get(0).getAuthor().login()).isEqualTo(BrennonUserLogin.of("anna@mail.com"));
        Assertions.assertThat(page.getContent().get(0).getReplies().get(1).getId()).isEqualTo(TicketReplyId.of("1"));
        Assertions.assertThat(page.getContent().get(0).getReplies().get(1).getText()).isEqualTo("reply 1 from 2 on ticket 1");
    }

    @Test
    @Transactional
    @DisplayName("Returns Ticket aggregate by id")
    public void givenDataExistsWhenRequestedByIdReturnsAggregate() {
        final var parameterSource = new MapSqlParameterSource();

        // init database
        jdbcTemplate.update(
                """
                INSERT INTO tickets(id, author_id, subject, text, status, created, updated)
                VALUES 
                    ('1', '1', 'Power outage in office 1', 'No power, please fix asap', 10, '2026-08-15 18:00:00+03', '2026-08-15 18:00:10+03'),
                    ('2', '1', 'Cant login', 'HR desk does not accept my creds', 30, '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '1', 'Slow internet', 'Cant attend live meetings', 20, '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '2', 'Printer problem', 'Printer got broken', 20, '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """, parameterSource);

        jdbcTemplate.update(
                """
                INSERT INTO ticket_replies(id, author_id, ticket_id, text, created, updated)
                VALUES 
                    ('1', '2', '1', 'reply 1 from 2 on ticket 1', '2026-08-15 18:00:05+03', '2026-08-15 18:00:10+03'),
                    ('2', '2', '1', 'reply 2 from 2 on ticket 1', '2026-08-15 13:00:00+03', '2026-08-15 13:01:45+03'),
                    ('3', '2', '2', 'reply 1 from 2 on ticket 2', '2026-08-15 13:30:00+03', '2026-08-15 13:36:00+03'),
                    ('4', '2', '2', 'reply 1 from 2 on ticket 2', '2026-08-15 15:00:00+03', '2026-08-15 16:00:00+03');
                """, parameterSource);

        var storage = new JdbcTicketStorage(jdbcTemplate);
        
        var ticket = storage.getTicketById(TicketId.of("1")).get();

        Assertions.assertThat(ticket).isNotNull();
        
        Assertions.assertThat(ticket.getId()).isEqualTo(TicketId.of("1"));
        Assertions.assertThat(ticket.getReplies()).hasSize(2);
        Assertions.assertThat(ticket.getReplies().get(0).getId()).isEqualTo(TicketReplyId.of("2"));
        Assertions.assertThat(ticket.getReplies().get(0).getText()).isEqualTo("reply 2 from 2 on ticket 1");
        Assertions.assertThat(ticket.getReplies().get(0).getAuthor().login()).isEqualTo(BrennonUserLogin.of("anna@mail.com"));
        Assertions.assertThat(ticket.getReplies().get(1).getId()).isEqualTo(TicketReplyId.of("1"));
        Assertions.assertThat(ticket.getReplies().get(1).getText()).isEqualTo("reply 1 from 2 on ticket 1");
    }


    @TestConfiguration
    public static class DbTestConfiguration {
        @Bean
        @ServiceConnection
        public PostgreSQLContainer postreSqlContainer() {
            PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"))
                .withDatabaseName("brennon")
                .withUsername("postgres")
                .withPassword("example")
                .withUrlParam("currentSchema", "brennon")
                .withInitScript("init.sql");
            return postgreSQLContainer;
        }
    }
}
