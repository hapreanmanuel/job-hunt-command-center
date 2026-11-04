package org.manu.jobhunt.persistence.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobEntityRepository extends JpaRepository<JobEntity, Long> {

    Optional<JobEntity> findByIdAndOwnerId(Long id, UUID ownerId);

    boolean existsByIdAndOwnerId(Long id, UUID ownerId);

    void deleteByIdAndOwnerId(Long id, UUID ownerId);

    List<JobEntity> findByOwnerIdAndArchivedFalseOrderByUpdatedAtDesc(UUID ownerId);

    List<JobEntity> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId);
}
