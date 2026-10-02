package no.loopacademy.repositories;

import no.loopacademy.models.camp.Camp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampRepository extends JpaRepository<Camp, String> {
}