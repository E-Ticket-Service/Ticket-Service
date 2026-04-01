package abb.tech.ticket_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
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
    @JoinColumn(name = "venue_id")
    Venue venue;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL)
    List<Section> sections = new ArrayList<>();

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    Integer capacity;

    Boolean hasSeatMap;
}