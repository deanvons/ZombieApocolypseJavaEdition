package actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import no.loopacademy.repositories.SurvivorRepository;
import no.loopacademy.services.SurvivorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;

public class ActionTests {
    private SurvivorService survivorService;
    private SurvivorRepository repository;

    @BeforeEach
    public void setup() {
        repository = mock(SurvivorRepository.class);
        Map<Long, Survivor> survivors = new LinkedHashMap<>();
        AtomicLong nextId = new AtomicLong(1);

        when(repository.save(any(Survivor.class))).thenAnswer(invocation -> {
            Survivor survivor = invocation.getArgument(0);
            if (survivor.getId() == null) {
                survivor.setId(nextId.getAndIncrement());
            }
            survivors.put(survivor.getId(), survivor);
            return survivor;
        });
        when(repository.findAll()).thenAnswer(invocation -> new ArrayList<>(survivors.values()));
        when(repository.findById(any(Long.class)))
                .thenAnswer(invocation -> Optional.ofNullable(survivors.get(invocation.getArgument(0))));
        when(repository.getReferenceById(any(Long.class)))
                .thenAnswer(invocation -> survivors.get(invocation.getArgument(0)));

        survivorService = new SurvivorService(repository);
    }

    @Test
    void shouldCreateActionWithCorrectValues() {
        // Arr
        String expectedName = "Drug";
        ActionType expectedActionType = ActionType.Disable;
        String expectedEffect = "Poes toe";
        String expectedTarget = "Yo mamma";
        AttributeWeights expectedAttributeWeights = new AttributeWeights();
        expectedAttributeWeights.setStrength(0.1);
        expectedAttributeWeights.setAgility(0.0);
        expectedAttributeWeights.setTrustworthiness(0.6);
        expectedAttributeWeights.setIntelligence(0.1);
        expectedAttributeWeights.setCourage(0.2);
        expectedAttributeWeights.setEndurance(0.0);
        expectedAttributeWeights.setLeadership(0.0);
        // Act
        Action a = new Action(expectedName,
                expectedActionType,
                expectedEffect,
                expectedTarget,
                expectedAttributeWeights);
        String actualName = a.getName();
        ActionType actualActionType = a.getType();
        AttributeWeights actualAttributeWeights = a.getAttributeWeights();
        String actualEffect = a.getEffect();
        String actualTarget = a.getTarget();
        // Ass
        assertEquals(expectedName, actualName);
        assertEquals(expectedActionType, actualActionType);
        assertEquals(expectedEffect, actualEffect);
        assertEquals(expectedTarget, actualTarget);
        assertEquals(expectedAttributeWeights, actualAttributeWeights);
    }

    @Test
    void shouldCalculateCorrectEffectivenessWithoutSkills() {
        double expectedEffectiveness = 52;
        String expectedName = "Kevin";
        survivorService.create(expectedName, SurvivorType.CAREGIVER);

        AttributeWeights drugWeights = new AttributeWeights();
        drugWeights.setStrength(0.1);
        drugWeights.setAgility(0.1);
        drugWeights.setTrustworthiness(0.1);
        drugWeights.setIntelligence(0.1);
        drugWeights.setCourage(0.1);
        drugWeights.setEndurance(0.1);
        drugWeights.setLeadership(0.4);
        Action drug = new Action("test", ActionType.Fix, "", "", drugWeights);

        long survivorId = survivorService.findById(1L).getId();

        double actualEffectiveness = survivorService.performAction(survivorId, drug);

        assertEquals(expectedEffectiveness, actualEffectiveness);
    }

}
