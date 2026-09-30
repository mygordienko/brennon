package com.mygordienko.brennon.ticket.di;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import com.mygordienko.brennon.core.auth.BrennonUserInfoApi;
import com.mygordienko.brennon.core.auth.internal.BrennonUserProfileService;
import com.mygordienko.brennon.core.common.ClockManager;
import com.mygordienko.brennon.ticket.adapter.jdbc.JdbcTicketStorage;
import com.mygordienko.brennon.ticket.adapter.jdbc.TicketCrudRepository;
import com.mygordienko.brennon.ticket.adapter.storage.LocalFileSystemTicketAttachmentStorage;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentService;
import com.mygordienko.brennon.ticket.internal.TicketAttachmentStoragePort;
import com.mygordienko.brennon.ticket.internal.TicketSecurityManager;
import com.mygordienko.brennon.ticket.internal.TicketService;
import com.mygordienko.brennon.ticket.internal.TicketStoragePort;

@Configuration
public class TicketServiceConfiguration {
    @Bean
    public TicketStoragePort ticketStoragePort(JdbcAggregateTemplate jdbcTemplate, JdbcClient jdbcClient, TicketCrudRepository ticketCrudRepository) {
        return new JdbcTicketStorage(jdbcTemplate, jdbcClient, ticketCrudRepository);
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
            TicketAttachmentStoragePort ticketAttachmentStoragePort,
            ClockManager clockManager) {
        return new TicketService(userProfileService, ticketStoragePort, ticketSecurityManager, ticketAttachmentStoragePort, clockManager);
    }

    @Bean 
    public TicketAttachmentStoragePort ticketAttachmentStoragePort(
            @Value("${brennon.ticket.attachments.storage.local.basefolder}") String basePath
            ) {
        return new LocalFileSystemTicketAttachmentStorage(Paths.get(basePath).toAbsolutePath());
    }

    @Bean 
    public TicketAttachmentService ticketAttachmentService(
            TicketStoragePort ticketStoragePort,
            TicketAttachmentStoragePort ticketAttachmentStoragePort, 
            TicketSecurityManager ticketSecurityManager,
            BrennonUserInfoApi userProfileService) {
        return new TicketAttachmentService(ticketStoragePort, ticketAttachmentStoragePort, ticketSecurityManager, userProfileService);
    }
}
