package org.manu.jobhunt.persistence.job;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.domain.job.Job;
import org.manu.jobhunt.domain.job.JobRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JobRepository on JPA. Only domain objects leave this class, so repository stay in this package. */
@Repository
@RequiredArgsConstructor
public class JobJpaRepository implements JobRepository {

    private final JobEntityRepository repository;

    @Override
    public Job save(Job job, UUID ownerId) {
        final JobEntity entity = new JobEntity();
        entity.setOwnerId(ownerId);
        entity.copyFrom(job);
        return repository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Job update(Job job, UUID ownerId) {
        final JobEntity entity = repository.findByIdAndOwnerId(job.getId(), ownerId).orElseThrow();
        entity.copyFrom(job);
        return repository.saveAndFlush(entity).toDomain();
    }

    @Override
    public Optional<Job> findById(Long id, UUID ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId).map(JobEntity::toDomain);
    }

    @Override
    public boolean existsById(Long id, UUID ownerId) {
        return repository.existsByIdAndOwnerId(id, ownerId);
    }

    @Override
    public void deleteById(Long id, UUID ownerId) {
        repository.deleteByIdAndOwnerId(id, ownerId);
    }

    @Override
    public List<Job> findAll(UUID ownerId, boolean includeArchived) {
        final List<JobEntity> found = includeArchived
                ? repository.findByOwnerIdOrderByUpdatedAtDesc(ownerId)
                : repository.findByOwnerIdAndArchivedFalseOrderByUpdatedAtDesc(ownerId);
        return found.stream().map(JobEntity::toDomain).toList();
    }
}
