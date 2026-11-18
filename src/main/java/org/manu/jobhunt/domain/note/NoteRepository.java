package org.manu.jobhunt.domain.note;

import java.util.List;
import java.util.UUID;

/** Every method takes the owner, so a query that forgets who's asking doesn't compile. */
public interface NoteRepository {
    Note save(Note note, Long jobId, UUID ownerId);
    boolean existsById(Long id, UUID ownerId);
    void deleteById(Long id, UUID ownerId);
    /** Newest first. */
    List<Note> findByJobId(Long jobId, UUID ownerId);
    /** Newest first. */
    List<Note> findAll(UUID ownerId);
}
