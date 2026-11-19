package com.mygordienko.brennon.core.common;

import org.springframework.core.io.Resource;

public record ResourceBundle(
    Resource resource,
    FileMeta meta
) {
    
}
