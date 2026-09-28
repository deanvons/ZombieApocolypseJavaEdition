package no.loopacademy.repositories;

import no.loopacademy.models.survivors.Survivor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurvivorRepository extends JpaRepository<Survivor, Long> {

}
