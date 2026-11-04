package org.manu.jobhunt.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The signed-in user as stored in the session: only our own user id. Serializable because
 * sessions are stored in Postgres.
 */
@AllArgsConstructor
@Getter
public class SignedInUser implements OAuth2User, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID userId;

    /** Spring Security's name for the principal; also the key Spring Session indexes sessions by. */
    @Override
    public String getName() {
        return userId.toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /** Kept minimal on purpose: provider data isn't needed after sign-in. */
    @Override
    public Map<String, Object> getAttributes() {
        return Map.of("userId", userId.toString());
    }
}
