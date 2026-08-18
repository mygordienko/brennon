package com.mygordienko.brennon.ticket.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;
import com.mygordienko.brennon.ticket.TicketSecurityManager;
import com.mygordienko.brennon.ticket.TicketService;
import com.mygordienko.brennon.ticket.TicketStoragePort;
import com.mygordienko.brennon.ticket.adapter.jdbc.JdbcTicketStorage;

@Configuration
public class TicketServiceConfiguration {
    @Bean
    public TicketStoragePort ticketStoragePort(NamedParameterJdbcTemplate jdbcTemplate) {
        return new JdbcTicketStorage(jdbcTemplate);
    }

    @Bean
    public TicketSecurityManager ticketSecurityManager() {
        return new TicketSecurityManager();
    }

    @Bean
    public TicketService ticketService(BrennonUserProfileService userProfileService, TicketStoragePort ticketStoragePort, TicketSecurityManager ticketSecurityManager) {
        return new TicketService(userProfileService, ticketStoragePort, ticketSecurityManager);
    }
}
