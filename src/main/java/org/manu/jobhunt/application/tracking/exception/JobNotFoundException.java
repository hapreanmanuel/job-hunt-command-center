package org.manu.jobhunt.application.tracking.exception;

import org.manu.jobhunt.application.common.exception.base.NotFoundException;

public class JobNotFoundException extends NotFoundException {
    public JobNotFoundException(Long id) {
        super("Job %d not found".formatted(id));
    }
}
