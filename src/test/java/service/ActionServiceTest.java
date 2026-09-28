package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.services.ActionService;

public class ActionServiceTest {
    ActionService actionService;

    @BeforeEach
    public void setup() {
        actionService = new ActionService();
    }

    @Test
    void shouldReturnFiveSampleActions() {
        assertEquals(5, actionService.findAll().size());
    }

    @Test
    void shouldFindActionById() {
        Action action = actionService.findById(1L);

        assertEquals("Attack", action.getName());
        assertEquals(ActionType.Attack, action.getType());
    }

    @Test
    void shouldReturnNullForUnknownActionId() {
        assertNull(actionService.findById(999L));
    }
}
