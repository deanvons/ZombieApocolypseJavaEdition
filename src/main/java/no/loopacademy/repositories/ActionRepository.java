package no.loopacademy.repositories;

import no.loopacademy.models.actions.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends JpaRepository<Action,Long> {
}
