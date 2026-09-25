package service;


import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.survivors.CareGiver;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorTypes;
import no.loopacademy.services.SurvivorService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class SurvivorServiceTest {
    @Test
    public void testSurvivorNotFound_shouldThrowException() {

        SurvivorService survivorService = new SurvivorService();

        assertThrows(SurvivorNotFoundException.class, () -> {
            survivorService.findById(99L);
        });
    }

    @Test
    void testCreateSurvivorWithCorrectType_checksCaregiverType_shouldPass() {
        CareGiver careGiver = new CareGiver("Tester");
        SurvivorService survivorService = new SurvivorService();

        Survivor survivor = survivorService.create("Tester2", SurvivorTypes.CAREGIVER);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test
    void testFindAllSurvivors_shouldReturnAllSurvivors_shouldPass() {
        SurvivorService survivorService = new SurvivorService();
        Survivor survivor1 = survivorService.create("Tester1", SurvivorTypes.CAREGIVER);
        Survivor survivor2 = survivorService.create("Tester2", SurvivorTypes.CAREGIVER);
        List<Survivor> expectedSurvivors = List.of(survivor1, survivor2);

        List<Survivor> actualsurvivors =  survivorService.findAll();

        assertEquals(expectedSurvivors, actualsurvivors);

    }

    @Test
    void testFindSurvivorById_passingSurvivorId_shouldReturnSurvivorById_shouldPass() {
        SurvivorService survivorService = new SurvivorService();
        Survivor expectedSurvivor = survivorService.create("Tester1", SurvivorTypes.CAREGIVER);
        expectedSurvivor.setId(1L);

        Survivor actualSurvivor = survivorService.findById(1L);

        assertEquals(expectedSurvivor, actualSurvivor);
    }
}
