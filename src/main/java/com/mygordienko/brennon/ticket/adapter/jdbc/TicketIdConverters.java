package com.mygordienko.brennon.ticket.adapter.jdbc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import com.mygordienko.brennon.ticket.FileId;
import com.mygordienko.brennon.ticket.TicketAttachmentId;
import com.mygordienko.brennon.ticket.TicketId;
import com.mygordienko.brennon.ticket.TicketReplyId;
import com.mygordienko.brennon.ticket.TicketStatus;

/**
 * Custom JDBC converters to be used by Spring Data JDBC when persisting/reading aggregates.
 */
public class TicketIdConverters {
    @WritingConverter
    public enum TicketIdToLongConverter implements Converter<TicketId, Long> {
        INSTANCE;
        @Override
        public Long convert(TicketId source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public enum LongToTicketIdConverter implements Converter<Long, TicketId> {
        INSTANCE;
        @Override
        public TicketId convert(Long source) {
            return TicketId.of(source);
        }
    }

    @WritingConverter
    public enum TicketReplyIdToLongConverter implements Converter<TicketReplyId, Long> {
        INSTANCE;
        @Override
        public Long convert(TicketReplyId source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public enum LongToTicketReplyIdConverter implements Converter<Long, TicketReplyId> {
        INSTANCE;
        @Override
        public TicketReplyId convert(Long source) {
            return TicketReplyId.of(source);
        }
    }

    /**
     * 
     * NOTE: Spring Data JDBC does not support mapping enums to custom Integer codes 
     *  (only mapping to ordinal position can be achieved with some trickery).
     */
    @WritingConverter
    public enum TicketStatusToStringConverter implements Converter<TicketStatus, String> {
        INSTANCE;
        @Override
        public String convert(TicketStatus source) {
            return source.getCode();
        }
    }

    /**
     * 
     * NOTE: Spring Data JDBC does not support mapping enums to custom Integer codes 
     *  (only mapping to ordinal position can be achieved with some trickery).
     */
    @ReadingConverter
    public enum StringToTicketStatusConverter implements Converter<String, TicketStatus> {
        INSTANCE;
        @Override
        public TicketStatus convert(String source) {
            return TicketStatus.of(source);
        }
    }

    @WritingConverter
    public enum TicketAttachmentIdToLongConverter implements Converter<TicketAttachmentId, Long> {
        INSTANCE;
        @Override
        public Long convert(TicketAttachmentId source) {
            return source.getValue();
        }
    }

    @ReadingConverter
    public enum LongToTicketAttachmentIdConverter implements Converter<Long, TicketAttachmentId> {
        INSTANCE;
        @Override
        public TicketAttachmentId convert(Long source) {
            return TicketAttachmentId.of(source);
        }
    }



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
