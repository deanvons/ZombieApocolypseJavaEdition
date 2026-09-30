package no.loopacademy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import no.loopacademy.models.survivors.Survivor;

@Repository
public interface SurvivorRepository extends JpaRepository<Survivor, Long> {

    boolean existsByName(String name);

}
