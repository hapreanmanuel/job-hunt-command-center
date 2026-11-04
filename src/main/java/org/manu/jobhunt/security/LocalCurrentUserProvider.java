package org.manu.jobhunt.security;

import org.manu.jobhunt.application.common.CurrentUserProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** The one fixed user of a local run. */
@Component
@Profile("local")
public class LocalCurrentUserProvider implements CurrentUserProvider {

    private final UUID userId;

    LocalCurrentUserProvider(@Value("${app.local-user-id}") UUID userId) {
        this.userId = userId;
    }

    @Override
    public UUID userId() {
        return userId;
    }
}
