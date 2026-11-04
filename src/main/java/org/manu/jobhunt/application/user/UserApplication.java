package org.manu.jobhunt.application.user;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.application.common.CurrentUserProvider;
import org.manu.jobhunt.application.user.command.SignInCommand;
import org.manu.jobhunt.domain.job.JobRepository;
import org.manu.jobhunt.domain.round.RoundRepository;
import org.manu.jobhunt.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Entry point of the user feature: the only class the API and security layers use for it. Covers
 * who may come in, and a person's rights over their data: to download it and to delete it.
 */
@Service
@RequiredArgsConstructor
public class UserApplication {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final RoundRepository roundRepository;
    private final CurrentUserProvider userProvider;

    /** Decides whether this person may sign in, registers them if they're new, and returns who they are. */
    @Transactional
    public UUID signIn(SignInCommand command) {
        userService.requireAllowed(command);
        final UUID userId = userService.userIdOf(command);
        userRepository.ensureExists(userId);
        return userId;
    }

    /** Creates the fixed user of a local run, where nobody signs in. Harmless if it already exists. */
    @Transactional
    public void ensureRegistered() {
        userRepository.ensureExists(userProvider.userId());
    }

    public UUID currentUserId() {
        return userProvider.userId();
    }

    @Transactional(readOnly = true)
    public UserData export() {
        final UUID ownerId = userProvider.userId();
        return new UserData(Instant.now(), jobRepository.findAll(ownerId, true), roundRepository.findAll(ownerId, true));
    }

    /** Removes the current user for good, with all their data. Signing in again starts empty. */
    @Transactional
    public void deleteUser() {
        userRepository.delete(userProvider.userId());
    }
}
