package org.manu.jobhunt.application.common.exception.base;

public abstract class NotFoundException extends RuntimeException {
    protected NotFoundException(String message) {
        super(message);
    }
}
