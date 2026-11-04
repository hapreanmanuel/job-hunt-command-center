package org.manu.jobhunt.domain.round;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Every method takes the owner, so a query that forgets who's asking doesn't compile. */
public interface RoundRepository {
    Round save(Round round, Long jobId, UUID ownerId);
    Round update(Round round, UUID ownerId);
    Optional<Round> findById(Long id, UUID ownerId);
    void deleteById(Long id, UUID ownerId);
    /** Oldest first, rounds without a date last. */
    List<Round> findByJobId(Long jobId, UUID ownerId);
    /** From inclusive, to exclusive. Leaves out archived jobs, like the calendar it feeds. */
    List<Round> findScheduledBetween(Instant from, Instant to, UUID ownerId);
    /** Oldest first, rounds without a date last. */
    List<Round> findAll(UUID ownerId, boolean includeArchived);
}
