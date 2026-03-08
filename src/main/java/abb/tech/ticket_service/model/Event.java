package abb.tech.ticket_service.model;

import abb.tech.ticket_service.enums.EventCategory;
import abb.tech.ticket_service.enums.EventStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class Event extends BaseEntity{

    String name;
    String description;
    Integer ageLimit;
    Double durationMinutes;
    String language;
    @ElementCollection
    @CollectionTable(name = "event_attachments", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "file_uuid")
    List<UUID> attachments;

    @Enumerated(EnumType.STRING)
    EventCategory category;

    @Enumerated(EnumType.STRING)
    EventStatus eventStatus;

    @OneToMany(mappedBy = "event")
    List<EventSession> sessions = new ArrayList<>();
}
