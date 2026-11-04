package org.manu.jobhunt.persistence.round;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundRepository;
import org.manu.jobhunt.persistence.job.JobEntityRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** RoundRepository on JPA. Only domain objects leave this class, so repository stay in this package. */
@Repository
@RequiredArgsConstructor
public class RoundJpaRepository implements RoundRepository {

    private final RoundEntityRepository repository;
    private final JobEntityRepository jobRepository;

    @Override
    public Round save(Round round, Long jobId, UUID ownerId) {
        final RoundEntity entity = new RoundEntity();
        entity.setOwnerId(ownerId);
        entity.setJob(jobRepository.getReferenceById(jobId));
        entity.copyFrom(round);
        return repository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Round update(Round round, UUID ownerId) {
        final RoundEntity entity = repository.findWithJobById(round.getId(), ownerId).orElseThrow();
        entity.copyFrom(round);
        return repository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Optional<Round> findById(Long id, UUID ownerId) {
        return repository.findWithJobById(id, ownerId).map(RoundEntity::toDomain);
    }

    @Override
    public void deleteById(Long id, UUID ownerId) {
        repository.deleteByIdAndOwnerId(id, ownerId);
    }

    @Override
    public List<Round> findByJobId(Long jobId, UUID ownerId) {
        return repository.findWithJobByJobId(jobId, ownerId).stream().map(RoundEntity::toDomain).toList();
    }

    @Override
    public List<Round> findScheduledBetween(Instant from, Instant to, UUID ownerId) {
        return repository.findWithJobScheduledBetween(from, to, ownerId).stream()
                .map(RoundEntity::toDomain).toList();
    }

    @Override
    public List<Round> findAll(UUID ownerId, boolean includeArchived) {
        return repository.findAllWithJob(ownerId, includeArchived).stream().map(RoundEntity::toDomain).toList();
    }
}
