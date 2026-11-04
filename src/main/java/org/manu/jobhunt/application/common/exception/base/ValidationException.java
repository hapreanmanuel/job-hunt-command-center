package org.manu.jobhunt.application.common.exception.base;

public abstract class ValidationException extends RuntimeException {
    protected ValidationException(String message) {
        super(message);
    }
}
