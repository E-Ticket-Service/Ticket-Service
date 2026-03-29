package abb.tech.ticket_service.enums;

import lombok.Getter;

@Getter
public enum TicketStatus {

    ACTIVE("Aktiv"),
    CANCELLED("Ləğv olunub"),
    DEACTIVATED("Deaktiv edilib");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

}
