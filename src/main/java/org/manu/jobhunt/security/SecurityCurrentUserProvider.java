package org.manu.jobhunt.security;

import org.manu.jobhunt.application.common.CurrentUserProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Who is calling, taken from the signed-in session. */
@Component
@Profile("!local")
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public UUID userId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SignedInUser user) {
            return user.getUserId();
        }
        return null;
    }
}
