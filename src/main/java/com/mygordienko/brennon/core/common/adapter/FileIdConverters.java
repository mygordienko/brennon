package com.mygordienko.brennon.core.common.adapter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import com.mygordienko.brennon.core.common.FileId;

/**
 * Custom JDBC converters for core common value types.
 */
public class FileIdConverters {
    @WritingConverter
    public enum FileIdToStringConverter implements Converter<FileId, String> {
        INSTANCE;
        @Override
        public String convert(FileId source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public enum StringToFileIdConverter implements Converter<String, FileId> {
        INSTANCE;
        @Override
        public FileId convert(String source) {
            return FileId.of(source);
        }
    }
}
