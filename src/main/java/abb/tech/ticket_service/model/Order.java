package abb.tech.ticket_service.model;

import abb.tech.ticket_service.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order extends BaseEntity{

    Long userId;

    BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    OrderStatus status;

    LocalDateTime createdAt;
}

