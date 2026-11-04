package org.manu.jobhunt.application.user.exception;

public class SignInNotInvitedException extends RuntimeException {
    public SignInNotInvitedException() {
        super("This account hasn't been invited");
    }
}
