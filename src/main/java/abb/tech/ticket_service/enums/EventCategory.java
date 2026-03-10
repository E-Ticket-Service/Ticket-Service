package abb.tech.ticket_service.enums;

import lombok.Getter;

@Getter
public enum EventCategory {
    CONCERT("Konsert"),
    CONFERENCE("Konfrans"),
    SPORTS("İdman"),
    THEATER("Teatr"),
    CINEMA("Kino"),
    EXHIBITION("Sərgi"),
    OTHER("Digər");

    private final String description;

    EventCategory(String description) {
        this.description = description;
    }

}
