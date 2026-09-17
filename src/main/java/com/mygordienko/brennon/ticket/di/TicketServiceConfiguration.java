package com.mygordienko.brennon.ticket.di;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.mygordienko.brennon.core.auth.BrennonUserInfoApi;
import com.mygordienko.brennon.core.auth.internal.BrennonUserProfileService;
import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.ticket.adapter.jdbc.JdbcTicketStorage;
import com.mygordienko.brennon.ticket.internal.TicketSecurityManager;
import com.mygordienko.brennon.ticket.internal.TicketService;
import com.mygordienko.brennon.ticket.internal.TicketStoragePort;

@Configuration
public class TicketServiceConfiguration {
    @Bean
    public TicketStoragePort ticketStoragePort(JdbcAggregateTemplate jdbcTemplate) {
        return new JdbcTicketStorage(jdbcTemplate);
    }

    @Bean
    public TicketSecurityManager ticketSecurityManager(BrennonUserInfoApi userProfileService) {
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
