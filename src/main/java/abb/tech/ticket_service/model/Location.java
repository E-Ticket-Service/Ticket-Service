package abb.tech.ticket_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "locations")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Location extends BaseEntity {

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String city;

    @Column(nullable = false)
    String address;

    String description;

    @OneToMany(mappedBy = "location")
    List<Hall> halls = new ArrayList<>();
}