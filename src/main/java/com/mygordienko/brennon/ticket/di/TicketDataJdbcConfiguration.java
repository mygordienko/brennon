package com.mygordienko.brennon.ticket.di;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import com.mygordienko.brennon.core.common.adapter.JdbcConverterProvider;
import com.mygordienko.brennon.ticket.adapter.jdbc.TicketIdConverters;

@Configuration
public class TicketDataJdbcConfiguration {
    @Bean
    public JdbcConverterProvider ticketJdbcConverterProvider() {
        return new JdbcConverterProvider() {
            @Override
            public Collection<Converter<?, ?>> getConverters() {
                return List.of(
                    TicketIdConverters.LongToTicketIdConverter.INSTANCE,
                    TicketIdConverters.TicketIdToLongConverter.INSTANCE,
                    TicketIdConverters.LongToTicketReplyIdConverter.INSTANCE,
                    TicketIdConverters.TicketReplyIdToLongConverter.INSTANCE,
                    TicketIdConverters.TicketStatusToStringConverter.INSTANCE,
                    TicketIdConverters.StringToTicketStatusConverter.INSTANCE
                );
            }
        };
    }
}
