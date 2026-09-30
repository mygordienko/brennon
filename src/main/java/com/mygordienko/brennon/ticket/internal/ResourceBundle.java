package com.mygordienko.brennon.ticket.internal;

import org.springframework.core.io.Resource;

import com.mygordienko.brennon.ticket.FileMeta;

public record ResourceBundle(
    Resource resource,
    FileMeta meta
) {
    
}
