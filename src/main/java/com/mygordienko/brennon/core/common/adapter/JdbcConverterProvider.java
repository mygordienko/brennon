package com.mygordienko.brennon.core.common.adapter;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;

import com.mygordienko.brennon.ticket.TicketId;

/**
 * Custom value types, ex {@link TicketId} require csutom conversion to be sucessfully persisted/read.
 * It is not sufficient to simply define a Converter bean, moreover such explicit declaration might
 * become a problem in the future because coverters are not necessarily the same for persistence and other
 * layers (ex. web).
 * 
 * Spring Data JDBC requires explicit converter registration via `AbstractJdbcConfiguration.userConverters()`.
 * And there can be only one such configuration, so all custom converters from different modules MUST be registered
 * together.
 * 
 * This interface is used by cetral JDBC configuration to gather all custom JDBC converters.
 * Modules are supposed to provide beans implementing this interface.
 */
public interface JdbcConverterProvider {
    Collection<Converter<?,?>> getConverters();
}
