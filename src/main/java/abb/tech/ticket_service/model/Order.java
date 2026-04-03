package abb.tech.ticket_service.model;

import abb.tech.ticket_service.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    OrderStatus orderStatus = OrderStatus.IN_PROGRESS;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    List<OrderItem> orderItems = new ArrayList<>();
}

