package service;


import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.services.SurvivorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SurvivorServiceTest {
    @Test
    public void SurvivorNotFoundTest_shouldThrowException() {

        SurvivorService survivorService = new SurvivorService();

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(99L);
        });
    }

    @Test
    void create() {
    }

    @Test
    void findAll() {
    }

    @Test
    void findById() {
    }
}
