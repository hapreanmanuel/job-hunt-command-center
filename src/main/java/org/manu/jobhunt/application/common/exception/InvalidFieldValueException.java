package org.manu.jobhunt.application.common.exception;

import org.manu.jobhunt.application.common.exception.base.ValidationException;

public class InvalidFieldValueException extends ValidationException {
    public InvalidFieldValueException(String fieldName, String problem) {
        super("%s %s".formatted(fieldName, problem));
    }
}
