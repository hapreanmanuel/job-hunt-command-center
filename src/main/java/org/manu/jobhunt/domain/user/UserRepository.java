package org.manu.jobhunt.domain.user;

import java.util.UUID;

/**
 * No User class goes with this: a user is only an id, because the app stores nothing else about
 * them. Add one when there is something to put in it.
 */
public interface UserRepository {
    /** Every record references its owner, so the row must exist before their first write. */
    void ensureExists(UUID id);
    /** Removes the user with everything they own, and ends every session they have open. */
    void delete(UUID id);
}
