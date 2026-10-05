package com.mygordienko.brennon.ticket;

public class TicketAttachmentId {
    private TicketAttachmentId(Integer value) {
        this.value = value;
    }

    public static final TicketAttachmentId of(Integer value) {
        return new TicketAttachmentId(value);
    }

    private final Integer value;

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public Integer getValue() {
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
        TicketAttachmentId other = (TicketAttachmentId) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }   

    
}
