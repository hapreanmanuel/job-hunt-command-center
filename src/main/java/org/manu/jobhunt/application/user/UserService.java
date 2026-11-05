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

    private static final String GITHUB = "github";

    private final Set<String> invitedEmails;
    private final Set<String> invitedGithubUsers;
    private final boolean open;

    /**
     * Written by hand (not @RequiredArgsConstructor), so the settings it reads are visible here.
     *
     * @param invitedEmails      comma-separated Google emails; empty lets nobody in
     * @param invitedGithubUsers comma-separated GitHub usernames; empty lets nobody in
     * @param open               whether anyone may sign in, invited or not
     */
    UserService(
            @Value("${app.access.invited-emails}") String invitedEmails,
            @Value("${app.access.invited-github-users}") String invitedGithubUsers,
            @Value("${app.access.open}") boolean open) {
        this.invitedEmails = listOf(invitedEmails);
        this.invitedGithubUsers = listOf(invitedGithubUsers);
        this.open = open;
    }

    void requireAllowed(SignInCommand command) {
        if (!open && !isInvited(command)) {
            throw new SignInNotInvitedException();
        }
    }

    UUID userIdOf(SignInCommand command) {
        final String identity = command.getProvider() + ":" + command.getProviderUserId();
        return UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
    }

    /** GitHub goes by username. For Google, an unverified email proves nothing, so it never matches. */
    private boolean isInvited(SignInCommand command) {
        if (GITHUB.equals(command.getProvider())) {
            final String username = command.getUsername();
            return username != null && invitedGithubUsers.contains(username.toLowerCase());
        }
        final String email = command.getEmail();
        return command.isEmailVerified() && email != null && invitedEmails.contains(email.toLowerCase());
    }

    private static Set<String> listOf(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(","))
                .map(entry -> entry.trim().toLowerCase())
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toSet());
    }
}
