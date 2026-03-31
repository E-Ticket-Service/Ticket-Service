package abb.tech.ticket_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "sections")
@NoArgsConstructor
@AllArgsConstructor
public class Section extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id")
    private Hall hall;

    private String name;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<Block> blocks = new ArrayList<>();

}