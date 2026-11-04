package org.manu.jobhunt.application.tracking.exception;

import org.manu.jobhunt.application.common.exception.base.NotFoundException;

public class RoundNotFoundException extends NotFoundException {
    public RoundNotFoundException(Long id) {
        super("Round %d not found".formatted(id));
    }
}
