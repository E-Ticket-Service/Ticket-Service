package abb.tech.ticket_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "buckets")
public class Bucket extends BaseEntity{

    Long userId;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "bucket", fetch = FetchType.LAZY)
    List<BucketItem> items;

}

