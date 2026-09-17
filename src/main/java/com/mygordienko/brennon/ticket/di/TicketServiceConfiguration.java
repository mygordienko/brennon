package com.mygordienko.brennon.ticket.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserProfileService;
import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.ticket.TicketSecurityManager;
import com.mygordienko.brennon.ticket.TicketService;
import com.mygordienko.brennon.ticket.TicketStoragePort;
import com.mygordienko.brennon.ticket.adapter.jdbc.JdbcTicketStorage;

@Configuration
public class TicketServiceConfiguration {
    @Bean
    public TicketStoragePort ticketStoragePort(JdbcAggregateTemplate jdbcTemplate) {
        return new JdbcTicketStorage(jdbcTemplate);
    }

    @Bean
    public TicketSecurityManager ticketSecurityManager(BrennonUserProfileService userProfileService) {
        return new TicketSecurityManager(userProfileService);
    }

    @Bean
    public TicketService ticketService(
            BrennonUserProfileService userProfileService, 
            TicketStoragePort ticketStoragePort, 
            TicketSecurityManager ticketSecurityManager,
            ClockManager clockManager) {
        return new TicketService(userProfileService, ticketStoragePort, ticketSecurityManager, clockManager);
    }
}
