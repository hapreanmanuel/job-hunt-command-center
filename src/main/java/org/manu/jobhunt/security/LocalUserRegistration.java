package org.manu.jobhunt.security;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.application.user.UserApplication;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Nobody signs in on a local run, so the fixed user is registered at startup. */
@Component
@Profile("local")
@RequiredArgsConstructor
class LocalUserRegistration implements ApplicationRunner {

    private final UserApplication userApplication;

    @Override
    public void run(ApplicationArguments arguments) {
        userApplication.ensureRegistered();
    }
}
