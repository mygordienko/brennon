package com.mygordienko.brennon.core.auth.adapter.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import com.mygordienko.brennon.core.auth.BrennonUserId;
import com.mygordienko.brennon.core.auth.BrennonUserLogin;

public class BrennonUserConverters {
    @WritingConverter
    public enum BrennonUserIdToStringConverter implements Converter<BrennonUserId, String> {
        INSTANCE;
        @Override
        public String convert(BrennonUserId source) {
            return source.toString();
        }
    }

    @ReadingConverter
    public enum StringToBrennonUserIdConverter implements Converter<String, BrennonUserId> {
        INSTANCE;
        @Override
        public BrennonUserId convert(String source) {
            return BrennonUserId.of(source);
        }
    }

    @WritingConverter
    public enum BrennonUserLoginToStringConverter implements Converter<BrennonUserLogin, String> {
        INSTANCE;
        @Override
        public String convert(BrennonUserLogin source) {
            return source.toString();
        }
    }

    @ReadingConverter
    public enum StringToBrennonUserLoginConverter implements Converter<String, BrennonUserLogin> {
        INSTANCE;
        @Override
        public BrennonUserLogin convert(String source) {
            return BrennonUserLogin.of(source);
        }
    }
}
