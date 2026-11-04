package org.manu.jobhunt.application.common.exception;

import org.manu.jobhunt.application.common.exception.base.ValidationException;

public class RequiredFieldMissingException extends ValidationException {
    public RequiredFieldMissingException(String fieldName) {
        super("%s is required".formatted(fieldName));
    }
}
