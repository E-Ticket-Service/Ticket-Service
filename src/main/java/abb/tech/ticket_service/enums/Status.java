package abb.tech.ticket_service.enums;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("Aktiv"), DELETED("Silinmiş");
    private final String description;

    Status(String description) {
        this.description = description;
    }
}
