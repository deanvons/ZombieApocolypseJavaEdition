package service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.SurvivorTypes;
import no.loopacademy.services.SurvivorService;

public class SurvivorServiceTest {

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
