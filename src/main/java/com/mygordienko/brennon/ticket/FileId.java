package com.mygordienko.brennon.ticket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileId {
    private FileId(String value) {
        this.value = value;
    }

    private static final Pattern FILE_ID_PATTERN = 
        Pattern.compile("^[a-z0-9-]+$");

    public static final FileId of(String value) {
        Matcher matcher = FILE_ID_PATTERN.matcher(value);
        
        if (matcher.matches()) {
            return new FileId(value);
        }
        
        throw new IllegalArgumentException("Invalid Uploaded File ID");
    }

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileId other = (FileId) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    } 
}
