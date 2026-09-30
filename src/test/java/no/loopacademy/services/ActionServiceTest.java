package no.loopacademy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.repositories.ActionRepository;

public class ActionServiceTest {
    ActionService actionService;
    ActionRepository actionRepository;

    @BeforeEach
    public void setup() {
        actionRepository = mock(ActionRepository.class);

        List<Action> actions = List.of(
                action(1L, "Attack", ActionType.Attack),
                action(2L, "Heal", ActionType.Heal),
                action(3L, "Scavenge", ActionType.Scavenge),
                action(4L, "Build Shelter", ActionType.Build),
                action(5L, "Persuade", ActionType.Persuade));

        when(actionRepository.findAll()).thenReturn(actions);
        when(actionRepository.findById(1L)).thenReturn(Optional.of(actions.getFirst()));
        when(actionRepository.findById(999L)).thenReturn(Optional.empty());

        actionService = new ActionService(actionRepository);
    }

    @Test
    void shouldReturnFiveSampleActions() {
        int expectedActionCount = 5;
        assertEquals(expectedActionCount, actionService.findAll().size());
    }

    @Test
    void shouldFindActionById() {
        Long actionId = 1L;
        String expectedActionName = "Attack";
        ActionType expectedActionType = ActionType.Attack;
        Action action = actionService.findById(actionId);

        assertEquals(expectedActionName, action.getName());
        assertEquals(expectedActionType, action.getType());
    }

    @Test
    void shouldThrowForUnknownActionId() {
        assertThrows(ActionNotFoundException.class, () -> actionService.findById(999L));
    }

    private Action action(Long id, String name, ActionType type) {
        String actionEffect = "";
        String actionTarget = "";
        AttributeWeights actionAttributeWeights = new AttributeWeights();
        Action action = new Action(name, type, actionEffect, actionTarget, actionAttributeWeights);
        action.setId(id);
        return action;
    }
}
