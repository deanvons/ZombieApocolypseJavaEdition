package no.loopacademy.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.exceptions.SurvivorNotFoundException;
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

    @Autowired 
    private MockMvc mockMvc;                //sends fake HTTP requests

    @MockitoBean 
    private SurvivorService survivorService; //fake service, w/o db
    

    //** GET requests */
    @Test
    void getSurvivorsShouldReturnListOfSurvivors() throws Exception {
        //Arrange: Setup fake service return value
        Survivor rick = new Survivor("Rick", SurvivorType.OUTLAW);
        when(survivorService.findAll()).thenReturn(List.of(rick));

        //Act + Assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Rick"))
            .andExpect(jsonPath("$[0].type").value("OUTLAW"));
    }

    @Test 
    void getSurvivorsShouldReturnEmptyList() throws Exception {
        //Arrange
        when(survivorService.findAll()).thenReturn(List.of());

        //Act + assert
        mockMvc.perform(get("/api/survivors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test 
    void getSurvivorByIdShouldReturnSurvivor() throws Exception {
        //Arrange
        Survivor jessica = new Survivor("Jessica", SurvivorType.HERO);
        jessica.setId(1L);
        when(survivorService.findById(1L)).thenReturn(jessica);

        //Act + assert
        mockMvc.perform(get("/api/survivors/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Jessica"))
            .andExpect(jsonPath("$.type").value("HERO"));
    }

    @Test
    void getSurvivorByIdShouldReturn404IfNotFound() throws Exception {
        //Arrange
        String errorMsg = "Survivor Not Found";
        when(survivorService.findById(1L))
            .thenThrow(new SurvivorNotFoundException(errorMsg));

        //Act + assert
        mockMvc.perform(get("/api/survivors/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value(errorMsg));
        
    }

    /** POST, PUT, DELETE requests */
    @Test
    void createSurvivor_ReturnsCreatedSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.TESTSURVIVOR);
        survivor.setId(1L);
        when(survivorService.create("Alice", SurvivorType.TESTSURVIVOR)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"type\":\"testsurvivor\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.type").value("TESTSURVIVOR"));

        verify(survivorService).create("Alice", SurvivorType.TESTSURVIVOR);
    }

    @Test
    void addSkill_ReturnsUpdatedSurvivor() throws Exception {
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill\":\"Accuracy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));

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
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        Tool medkit = new Tool("Medkit", 2.5, 10);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"tool\",\"name\":\"Medkit\",\"weight\":2.5,\"durability\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"));

        verify(survivorService).loadItem(1L, medkit);
    }

    @Test
    void performAction_ReturnsActionEffectiveness() throws Exception {
        ActionResult result = new ActionResult(null, null, 72.5);
        when(survivorService.performAction(1L, 2L)).thenReturn(result);

        mockMvc.perform(post("/api/survivors/1/actions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.survivorId").value(1))
                .andExpect(jsonPath("$.actionId").value(2))
                .andExpect(jsonPath("$.effectiveness").value(72.5));

        verify(survivorService).performAction(1L, 2L);
    }
}
