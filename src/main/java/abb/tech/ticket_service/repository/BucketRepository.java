package abb.tech.ticket_service.repository;

import abb.tech.ticket_service.model.Bucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BucketRepository extends JpaRepository<Bucket, Long> {

    Optional<Bucket> findByUserId(Long userId);
}
