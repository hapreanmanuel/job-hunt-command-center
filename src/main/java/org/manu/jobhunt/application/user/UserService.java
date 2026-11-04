package org.manu.jobhunt.application.user;

import org.manu.jobhunt.application.user.command.SignInCommand;
import org.manu.jobhunt.application.user.exception.SignInNotInvitedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Who may sign in, and which user they are. Package-private: only UserApplication uses it.
 * <p>
 * The user id is derived from the provider's stable id, so the app needs no lookup before anyone
 * is signed in, and stores no email address at all.
 */
@Service
class UserService {

    private final Set<String> invitedEmails;
    private final boolean open;

    /**
     * Written by hand (not @RequiredArgsConstructor), so the settings it reads are visible here.
     *
     * @param invitedEmails comma-separated; empty lets nobody in
     * @param open          whether anyone may sign in, invited or not
     */
    UserService(
            @Value("${app.access.invited-emails}") String invitedEmails,
            @Value("${app.access.open}") boolean open) {
        this.invitedEmails = Arrays.stream(invitedEmails.split(","))
                .map(email -> email.trim().toLowerCase())
                .filter(email -> !email.isEmpty())
                .collect(Collectors.toSet());
        this.open = open;
    }

    /** An unverified email proves nothing, so it never matches an invitation. */
    void requireAllowed(SignInCommand command) {
        if (open) {
            return;
        }
        final String email = command.getEmail();
        if (!command.isEmailVerified() || email == null || !invitedEmails.contains(email.toLowerCase())) {
            throw new SignInNotInvitedException();
        }
    }

    UUID userIdOf(SignInCommand command) {
        final String identity = command.getProvider() + ":" + command.getProviderUserId();
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }
}
