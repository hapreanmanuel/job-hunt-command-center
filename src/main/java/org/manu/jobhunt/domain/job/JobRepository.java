package org.manu.jobhunt.domain.job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Every method takes the owner, so a query that forgets who's asking doesn't compile. */
public interface JobRepository {
    Job save(Job job, UUID ownerId);
    Job update(Job job, UUID ownerId);
    Optional<Job> findById(Long id, UUID ownerId);
    boolean existsById(Long id, UUID ownerId);
    void deleteById(Long id, UUID ownerId);
    /** Newest-updated first. */
    List<Job> findAll(UUID ownerId, boolean includeArchived);
}
