package org.manu.jobhunt.persistence.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.manu.jobhunt.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Plain SQL through the EntityManager, unlike the other repositories. A user is only an id, so
 * there is no entity to map, and neither ON CONFLICT nor clearing sessions exists in JPA.
 */
@Repository
public class UserJpaRepository implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void ensureExists(UUID id) {
        entityManager.createNativeQuery("INSERT INTO app.app_user (id) VALUES (:id) ON CONFLICT DO NOTHING")
                .setParameter("id", id)
                .executeUpdate();
    }

    /** Deleting the user row cascades to their jobs, and from those to their rounds. */
    @Override
    public void delete(UUID id) {
        entityManager.createNativeQuery("DELETE FROM app.spring_session WHERE principal_name = :name")
                .setParameter("name", id.toString())
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM app.app_user WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
