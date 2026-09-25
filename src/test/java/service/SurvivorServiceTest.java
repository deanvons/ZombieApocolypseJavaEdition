package service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.CareGiver;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorTypes;
import no.loopacademy.services.SurvivorService;
import org.junit.jupiter.api.Test;

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

        List<Survivor> actualsurvivors = survivorService.findAll();

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

    @Test
    void survivorService_addSkillToSurvivor_survivorWithNewSkill() {
        SurvivorService survivorService = new SurvivorService();
        survivorService.create("CareGiver", SurvivorTypes.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.PsychologicalSupport));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(1L, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void survivorService_addSkillToSurvivorThatItAlreadyHas_noChangeInSurvivorSkills() {
        SurvivorService survivorService = new SurvivorService();
        survivorService.create("CareGiver", SurvivorTypes.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.PsychologicalSupport));

        survivorService.addSkill(1L, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void survivorService_removeSkillFromSurvivor_survivorWithoutSkill() {
        SurvivorService survivorService = new SurvivorService();
        survivorService.create("CareGiver", SurvivorTypes.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine));

        survivorService.removeSkill(1L, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

}
