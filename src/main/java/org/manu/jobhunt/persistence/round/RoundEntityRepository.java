package org.manu.jobhunt.persistence.round;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Queries fetch the job with the round, because a domain Round carries its job. */
public interface RoundEntityRepository extends JpaRepository<RoundEntity, Long> {

    void deleteByIdAndOwnerId(Long id, UUID ownerId);

    @Query("select r from RoundEntity r join fetch r.job where r.id = :id and r.ownerId = :ownerId")
    Optional<RoundEntity> findWithJobById(@Param("id") Long id, @Param("ownerId") UUID ownerId);

    @Query("select r from RoundEntity r join fetch r.job j where j.id = :jobId and r.ownerId = :ownerId "
            + "order by r.scheduledAt asc nulls last, r.id asc")
    List<RoundEntity> findWithJobByJobId(@Param("jobId") Long jobId, @Param("ownerId") UUID ownerId);

    @Query("select r from RoundEntity r join fetch r.job j where r.ownerId = :ownerId "
            + "and j.archived = false and r.scheduledAt >= :from and r.scheduledAt < :to "
            + "order by r.scheduledAt asc")
    List<RoundEntity> findWithJobScheduledBetween(
            @Param("from") Instant from, @Param("to") Instant to, @Param("ownerId") UUID ownerId);

    @Query("select r from RoundEntity r join fetch r.job j where r.ownerId = :ownerId "
            + "and (:includeArchived = true or j.archived = false) "
            + "order by r.scheduledAt asc nulls last, r.id asc")
    List<RoundEntity> findAllWithJob(
            @Param("ownerId") UUID ownerId, @Param("includeArchived") boolean includeArchived);
}
