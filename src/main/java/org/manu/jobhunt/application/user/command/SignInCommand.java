package org.manu.jobhunt.application.user.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** What the sign-in provider says about who signed in. */
@AllArgsConstructor
@Builder
@Getter
public class SignInCommand {

    private String provider;
    /** The provider's stable id for this person. Emails can change, this can't. */
    private String providerUserId;
    private String email;
    private boolean emailVerified;
}
