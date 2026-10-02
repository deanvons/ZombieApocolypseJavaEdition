package no.loopacademy.controllers;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.exceptions.ActionNotFoundException;
import no.loopacademy.mappers.ActionMapperImpl;
import no.loopacademy.models.actions.Action;
import no.loopacademy.models.actions.ActionType;
import no.loopacademy.models.actions.RequiredItem;
import no.loopacademy.models.attributes.AttributeWeights;
import no.loopacademy.services.ActionService;

@WebMvcTest(ActionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ActionMapperImpl.class)   // real MapStruct mapper, so the JSON shape is the real one
class ActionControllerTest {

    private String actionName;
    private String expectedActionName;
    private String expectedActionType;
    private String actionEffect;
    private String actionTarget;
    private ActionType actionType;
    private AttributeWeights actionAttributeWeights;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActionService actionService;


    @BeforeEach
    void setup() {
        actionName = "GenericActionName";
        expectedActionName = actionName;
        actionType = ActionType.Attack;
        expectedActionType = actionType.name();
        actionEffect = "GenericActionEffect";
        actionTarget = "GenericActionTarget";
        actionAttributeWeights = null;
    }

    @Test
    void getActionsShouldReturnListOfActions() throws Exception {
        //Arrange
        int expectedActionCount = 1;
        Action attack = new Action(actionName, actionType, actionEffect, actionTarget, actionAttributeWeights);
        when(actionService.findAll()).thenReturn(List.of(attack));

        //Act + assert
        mockMvc.perform(get("/api/actions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedActionCount))
            .andExpect(jsonPath("$[0].name").value(expectedActionName))
            .andExpect(jsonPath("$[0].type").value(expectedActionType))
            .andExpect(jsonPath("$[0].requiredItems").isArray())     // [] rather than null when nothing is required
            .andExpect(jsonPath("$[0].requiredItems").isEmpty());
    }

    @Test
    void getActionsShouldReturnEmptyList() throws Exception {
        //Arrange
        int expectedActionCount = 0;
        when(actionService.findAll()).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/actions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedActionCount));
    }

    @Test
    void getActionByIdShouldReturnAction() throws Exception {
        //Arrange
        Long expectedActionId = 1L;
        Action attack = new Action(actionName, actionType, actionEffect, actionTarget, actionAttributeWeights);
        attack.setId(expectedActionId);
        when(actionService.findById(expectedActionId)).thenReturn(attack);

        //Act + assert
        mockMvc.perform(get("/api/actions/" + expectedActionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedActionId))
            .andExpect(jsonPath("$.name").value(expectedActionName))
            .andExpect(jsonPath("$.type").value(expectedActionType));
    }

    @Test
    void getActionByIdShouldReturnRequiredItems() throws Exception {
        //Arrange
        Long actionId = 1L;
        Action heal = new Action(actionName, actionType, actionEffect, actionTarget, actionAttributeWeights);
        heal.setId(actionId);
        heal.getRequiredItems().add(new RequiredItem("weapon", null));
        heal.getRequiredItems().add(new RequiredItem("tool", "First Aid Kit"));
        when(actionService.findById(actionId)).thenReturn(heal);

        //Act + assert
        mockMvc.perform(get("/api/actions/" + actionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requiredItems.length()").value(2))
            .andExpect(jsonPath("$.requiredItems[0].type").value("weapon"))
            .andExpect(jsonPath("$.requiredItems[0].name").value(nullValue()))
            .andExpect(jsonPath("$.requiredItems[1].type").value("tool"))
            .andExpect(jsonPath("$.requiredItems[1].name").value("First Aid Kit"));
    }


    @Test
    void getActionByIdShouldThrowNoActionFoundError() throws Exception {
        //Arrange
        Long actionId = 1L;
        int expectedStatusCode = 404;
        String expectedErrorMessage = "Action not found";
        when(actionService.findById(actionId))
            .thenThrow(new ActionNotFoundException(expectedErrorMessage));

        //Act + assert
        mockMvc.perform(get("/api/actions/" + actionId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(expectedStatusCode))
            .andExpect(jsonPath("$.message").value(expectedErrorMessage));
    }
}
