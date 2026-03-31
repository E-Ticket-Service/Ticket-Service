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
@Table(name = "rows")
@NoArgsConstructor
@AllArgsConstructor
public class Row extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block block;

    private Integer rowNumber;

    @OneToMany(mappedBy = "row", cascade = CascadeType.ALL)
    private List<Seat> seats = new ArrayList<>();
}