package com.mygordienko.brennon.core.common.di;

import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import com.mygordienko.brennon.core.common.adapter.FileIdConverters;
import com.mygordienko.brennon.core.common.adapter.JdbcConverterProvider;

@Configuration
public class CommonDataJdbcConfiguration {
    @Bean
    public JdbcConverterProvider commonJdbcConverterProvider() {
        return new JdbcConverterProvider() {
            @Override
            public Collection<Converter<?, ?>> getConverters() {
                return List.of(
                    FileIdConverters.FileIdToStringConverter.INSTANCE,
                    FileIdConverters.StringToFileIdConverter.INSTANCE
                );
            }
        };
    }
}
