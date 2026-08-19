package com.mygordienko.brennon.core.common.di;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import com.mygordienko.brennon.core.common.adapter.JdbcConverterProvider;

@Configuration
public class DataJdbcConfig extends AbstractJdbcConfiguration {

    @Autowired
    private List<JdbcConverterProvider> providers;

    /**
     * Custom JDBC conversion configuration. Use `userConveters()` to add custom converters keeping default once.
     */
    @Override
    protected List<?> userConverters() {
        return providers.stream()
            .flatMap( provider -> provider.getConverters().stream())
            .toList();
    }
}
