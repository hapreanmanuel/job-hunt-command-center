package org.manu.jobhunt.persistence.note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteEntityRepository extends JpaRepository<NoteEntity, Long> {

    boolean existsByIdAndOwnerId(Long id, UUID ownerId);

    void deleteByIdAndOwnerId(Long id, UUID ownerId);

    List<NoteEntity> findByJobIdAndOwnerIdOrderByCreatedAtDescIdDesc(Long jobId, UUID ownerId);

    List<NoteEntity> findByOwnerIdOrderByCreatedAtDescIdDesc(UUID ownerId);
}
