package org.manu.jobhunt.persistence.note;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.domain.note.Note;
import org.manu.jobhunt.domain.note.NoteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** NoteRepository on JPA. Only domain objects leave this class. */
@Repository
@RequiredArgsConstructor
public class NoteJpaRepository implements NoteRepository {

    private final NoteEntityRepository repository;

    @Override
    public Note save(Note note, Long jobId, UUID ownerId) {
        final NoteEntity entity = new NoteEntity();
        entity.setOwnerId(ownerId);
        entity.setJobId(jobId);
        entity.setText(note.getText());
        return repository.saveAndFlush(entity).toDomain();
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
    public List<Note> findByJobId(Long jobId, UUID ownerId) {
        return repository.findByJobIdAndOwnerIdOrderByCreatedAtDescIdDesc(jobId, ownerId).stream()
                .map(NoteEntity::toDomain).toList();
    }

    @Override
    public List<Note> findAll(UUID ownerId) {
        return repository.findByOwnerIdOrderByCreatedAtDescIdDesc(ownerId).stream().map(NoteEntity::toDomain).toList();
    }
}
