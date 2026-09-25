package service;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;

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
        Survivor careGiver = new Survivor("Tester", SurvivorType.CAREGIVER);
        SurvivorService survivorService = new SurvivorService();

        Survivor survivor = survivorService.create("Tester2", SurvivorType.CAREGIVER);

        assertEquals(careGiver.getClass(), survivor.getClass());
    }

    @Test
    void testFindAllSurvivors_shouldReturnAllSurvivors_shouldPass() {
        SurvivorService survivorService = new SurvivorService();
        Survivor survivor1 = survivorService.create("Tester1", SurvivorType.CAREGIVER);
        Survivor survivor2 = survivorService.create("Tester2", SurvivorType.CAREGIVER);
        List<Survivor> expectedSurvivors = List.of(survivor1, survivor2);

        List<Survivor> actualsurvivors = survivorService.findAll();

        assertEquals(expectedSurvivors, actualsurvivors);

    }

    @Test
    void testFindSurvivorById_passingSurvivorId_shouldReturnSurvivorById_shouldPass() {
        SurvivorService survivorService = new SurvivorService();
        Survivor expectedSurvivor = survivorService.create("Tester1", SurvivorType.CAREGIVER);
        expectedSurvivor.setId(1L);

        Survivor actualSurvivor = survivorService.findById(1L);

        assertEquals(expectedSurvivor, actualSurvivor);
    }

    @Test
    void testAddSkillToSurvivor_survivor_survivorWithNewSkill() {
        SurvivorService survivorService = new SurvivorService();
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));
        expectedOutput.add(Skill.Accuracy);

        survivorService.addSkill(1L, Skill.Accuracy);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void testAddSkillToSurvivorThatItAlreadyHas_survivorWithSkill_noChangeInSurvivorSkills() {
        SurvivorService survivorService = new SurvivorService();
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(
                List.of(Skill.FieldMedicine, Skill.PsychologicalSupport, Skill.Cooking));

        survivorService.addSkill(1L, Skill.FieldMedicine);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void testRemoveSkillFromSurvivor_survivorWithSkill_survivorWithoutSkill() {
        SurvivorService survivorService = new SurvivorService();
        survivorService.create("CareGiver", SurvivorType.CAREGIVER);
        List<Skill> expectedOutput = new ArrayList<>(List.of(Skill.FieldMedicine, Skill.Cooking));

        survivorService.removeSkill(1L, Skill.PsychologicalSupport);
        List<Skill> actualOutput = survivorService.findById(1L).getSkills();

        assertEquals(expectedOutput, actualOutput);
    }

}
