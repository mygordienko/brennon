package com.mygordienko.brennon.ticket.adapter.rest;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.io.CacheMemoryBudget;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;

public class FileContentValidator {
    public InputStream checkRealFileType(InputStream originalStream) throws IOException {
        TikaInputStream bis = TikaInputStream.get(originalStream);
        bis.enableRewind(new CacheMemoryBudget(1024));
        
        DefaultDetector detector = new DefaultDetector();
        MediaType mediaType = detector.detect(bis, new Metadata(), new ParseContext());
        
        bis.rewind();
        
        if (mediaType.getType().equals("image")) {
            return bis;
        }

        throw new IllegalArgumentException("File type not supported: " + mediaType);
    }
}
