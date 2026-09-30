package no.loopacademy.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.dtos.request.ItemLoadRequest;
import no.loopacademy.dtos.response.ItemResponse;
import no.loopacademy.dtos.response.SurvivorResponse;
import no.loopacademy.mappers.ItemMapper;
import no.loopacademy.mappers.SurvivorMapper;
import no.loopacademy.mappers.ItemMapperImpl;
import no.loopacademy.mappers.SurvivorMapperImpl;
import no.loopacademy.models.actions.ActionResult;
import no.loopacademy.models.items.Tool;
import no.loopacademy.models.skills.Skill;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;

@WebMvcTest(SurvivorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SurvivorMapperImpl.class, ItemMapperImpl.class})   // real MapStruct mappers, so the JSON shape is the real one
class SurvivorControllerTest {
    String survivorName;
    String expectedSurvivorName;
    SurvivorType survivorType;
    String responseType;
    String expectedSurvivorTypeString;
    SurvivorType expectedSurvivorType;

    String itemMedkit;
    Double itemMedkitWeight;
    Double itemMedkitDurability;

    @Autowired 
    private MockMvc mockMvc;                //sends fake HTTP requests

    @MockitoBean 
    private SurvivorService survivorService; //fake service, w/o db


    @BeforeEach
    public void setup(){
        survivorName = "Rick";
        expectedSurvivorName = "Rick";
        survivorType = SurvivorType.OUTLAW;
        responseType = "OUTLAW";
        expectedSurvivorType = SurvivorType.OUTLAW;
        expectedSurvivorTypeString = "OUTLAW";

        itemMedkit = "Medkit";
        itemMedkitWeight = 2.5;
        itemMedkitDurability = 10.0;
    }

    //** GET requests */
    @Test
    void getSurvivorsShouldReturnListOfSurvivors() throws Exception {
        //Arrange: Setup fake service return value
        int expectedSurvivorCount = 1;
        Survivor rick = new Survivor(survivorName, survivorType);
        when(survivorService.findAll()).thenReturn(List.of(rick));

        //Act + Assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedSurvivorCount))
            .andExpect(jsonPath("$[0].name").value(expectedSurvivorName));
    }

    @Test 
    void getSurvivorsShouldReturnEmptyList() throws Exception {
        //Arrange
        int expectedSurvivorCount = 0;
        when(survivorService.findAll()).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(expectedSurvivorCount));
    }

    @Test 
    void getSurvivorByIdShouldReturnSurvivor() throws Exception {
        //Arrange
        Survivor jessica = new Survivor(survivorName, survivorType);
        when(survivorService.findById(1L)).thenReturn(jessica);

        //Act + assert
        mockMvc.perform(get("/api/survivors/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(expectedSurvivorName));
    }

    @Test
    void getSurvivorByIdShouldReturn404IfNotFound() throws Exception {
        //Arrange
        int expectedStatusCode = 404;
        String expectedErrorMessage = "Survivor Not Found";
        when(survivorService.findById(1L))
            .thenThrow(new SurvivorNotFoundException(expectedErrorMessage));

        //Act + assert
        mockMvc.perform(get("/api/survivors/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(expectedStatusCode))
            .andExpect(jsonPath("$.message").value(expectedErrorMessage));
        
    }

    /** POST, PUT, DELETE requests */
    @Test
    void createSurvivor_ReturnsCreatedSurvivor() throws Exception {
        Long survivorId = 1L;
        Long expectedSurvivorId = 1L;
        Survivor survivor = new Survivor(survivorName, survivorType);
        survivor.setId(survivorId);
        // SurvivorResponse response = response(expectedSurvivorId, survivorName, expectedSurvivorType);
        when(survivorService.create(survivorName, survivorType)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + survivorName
                                + "\",\"type\":\"" + expectedSurvivorTypeString + "\"}"))
                .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(expectedSurvivorId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(survivorName))
            .andExpect(jsonPath("$.type").value(expectedSurvivorTypeString));

        verify(survivorService).create(expectedSurvivorName, survivorType);
    }

    @Test
    void addSkill_ReturnsUpdatedSurvivor() throws Exception {
        Survivor survivor = new Survivor(survivorName, survivorType);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill\":\"Accuracy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(expectedSurvivorName));

        verify(survivorService).addSkill(1L, Skill.Accuracy);
        verify(survivorService).findById(1L);
    }

    @Test
    void deleteSurvivorSkill_RemovesSkillFromSurvivor() throws Exception {
        mockMvc.perform(delete("/api/survivors/1/skills/PsychologicalSupport"))
                .andExpect(status().isNoContent());

        verify(survivorService).removeSkill(1L, Skill.PsychologicalSupport);
    }

    @Test
    void loadItem_ReturnsUpdatedSurvivor() throws Exception {

        SurvivorType survivorType = SurvivorType.CAREGIVER;

        Survivor survivor = new Survivor(survivorName, survivorType);

        Tool medkit = new Tool(itemMedkit, itemMedkitWeight, itemMedkitDurability);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"tool\",\"name\":\"" + itemMedkit
                                + "\",\"weight\":" + itemMedkitWeight
                                + ",\"durability\":" + itemMedkitDurability + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(expectedSurvivorName));

        verify(survivorService).loadItem(1L, medkit);
    }

    @Test
    void performAction_ReturnsActionEffectiveness() throws Exception {
        Long expectedSurvivorId = 1L;
        Long expectedActionId = 2L;
        double expectedEffectiveness = 72.5;
        ActionResult result = new ActionResult(null, null, expectedEffectiveness);
        when(survivorService.performAction(expectedSurvivorId, expectedActionId)).thenReturn(result);

        mockMvc.perform(post("/api/survivors/" + expectedSurvivorId + "/actions/" + expectedActionId))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.survivorId").value(expectedSurvivorId))
            .andExpect(jsonPath("$.actionId").value(expectedActionId))
            .andExpect(jsonPath("$.effectiveness").value(expectedEffectiveness));

        verify(survivorService).performAction(1L, 2L);
    }

}
