package abb.tech.ticket_service.enums;

import lombok.Getter;

@Getter
public enum SeatStatus {
    AVAILABLE("Boş"),
    RESERVED("Rezerv edilib"),
    SOLD("Satılıb");

    private final String description;

    SeatStatus(String description) {
        this.description = description;
    }

}
