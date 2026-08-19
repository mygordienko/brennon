package com.mygordienko.brennon.core.auth.di;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import com.mygordienko.brennon.core.auth.adapter.jdbc.BrennonUserConverters;
import com.mygordienko.brennon.core.common.adapter.JdbcConverterProvider;

@Configuration
public class BrennonUserDataJdbcConfiguration {
    @Bean
    public JdbcConverterProvider userJdbcConverterProvider() {
        return new JdbcConverterProvider() {
            @Override
            public Collection<Converter<?, ?>> getConverters() {
                return List.of(
                    BrennonUserConverters.BrennonUserIdToStringConverter.INSTANCE,
                    BrennonUserConverters.StringToBrennonUserIdConverter.INSTANCE,
                    BrennonUserConverters.BrennonUserLoginToStringConverter.INSTANCE,
                    BrennonUserConverters.StringToBrennonUserLoginConverter.INSTANCE
                );
            }
        };
    }
}
