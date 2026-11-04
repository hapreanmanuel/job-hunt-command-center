package org.manu.jobhunt.application.common;

import java.util.UUID;

/** Who is calling. The id is null when nobody has signed in, for example during startup. */
public interface CurrentUserProvider {
    UUID userId();
}
