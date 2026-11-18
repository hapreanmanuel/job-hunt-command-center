package org.manu.jobhunt.application.tracking.exception;

import org.manu.jobhunt.application.common.exception.base.NotFoundException;

public class NoteNotFoundException extends NotFoundException {
    public NoteNotFoundException(Long id) {
        super("Note %d not found".formatted(id));
    }
}
