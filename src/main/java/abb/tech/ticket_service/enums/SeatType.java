package abb.tech.ticket_service.enums;

import lombok.Getter;

@Getter
public enum SeatType {
    VIP("VIP"),
    STANDARD("Standart"),
    BALCONY("Balkon");

    private final String description;

    SeatType(String description) {
        this.description = description;
    }
}
