package abb.tech.ticket_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Entity
@Table(name = "halls")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Hall extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    Location location;

    @OneToMany(mappedBy = "hall", fetch = FetchType.LAZY)
    List<Seat> seats;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    Integer capacity;

    Boolean hasSeatMap;
}