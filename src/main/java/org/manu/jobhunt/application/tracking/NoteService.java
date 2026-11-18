package org.manu.jobhunt.application.tracking;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.application.tracking.exception.NoteNotFoundException;
import org.manu.jobhunt.domain.note.Note;
import org.manu.jobhunt.domain.note.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.manu.jobhunt.application.common.Validations.requireMaxLength;
import static org.manu.jobhunt.application.common.Validations.requireNonBlank;

/**
 * Note validation and building notes. Package-private: only TrackingApplication uses it.
 */
@Service
@RequiredArgsConstructor
class NoteService {

    private static final int MAX_TEXT_LENGTH = 20_000;

    private final NoteRepository noteRepository;

    void requireExists(Long id, UUID ownerId) {
        if (!noteRepository.existsById(id, ownerId)) {
            throw new NoteNotFoundException(id);
        }
    }

    void validate(String text) {
        requireNonBlank(text, "text");
        requireMaxLength(text, MAX_TEXT_LENGTH, "text");
    }

    Note build(String text) {
        return Note.builder().text(text.strip()).build();
    }
}
