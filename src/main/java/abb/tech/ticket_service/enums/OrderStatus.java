package abb.tech.ticket_service.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    IN_PROGRESS("İcradadır"),
    COMPLETED("Tamamlandı"),
    CANCELLED("Ləğv edildi");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
