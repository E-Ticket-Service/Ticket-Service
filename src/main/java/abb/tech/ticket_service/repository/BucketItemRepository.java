package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.BucketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BucketItemRepository extends JpaRepository<BucketItem, Long> {

    Optional<BucketItem> findByBucketIdAndEventSessionIdAndSeatId(Long bucketId, Long eventSessionId, Long seatId);

    List<BucketItem> findByBucketId(Long bucketId);

    List<BucketItem> findByBucketIdAndSelectedTrue(Long bucketId);
}
