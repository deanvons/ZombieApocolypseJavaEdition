package no.loopacademy.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import no.loopacademy.exceptions.SurvivorNotFoundException;
import no.loopacademy.config.authConfig;
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
@Import({ SurvivorMapperImpl.class, ItemMapperImpl.class, authConfig.class }) // real MapStruct mappers, so the JSON
class SurvivorControllerTest {
    private static final String KEYCLOAK_ID = "3f2a9c1e-0000-4000-8000-000000000001";

    @Autowired 
    private MockMvc mockMvc;                //sends fake HTTP requests

    @MockitoBean 
    private SurvivorService survivorService; //fake service, w/o db
    
    @AfterEach
    void clearSecurityContext() { // Clear jwt token before each test
        SecurityContextHolder.clearContext();
    }

    //** GET requests */
    @Test
    void getSurvivorsShouldReturnListOfSurvivors() throws Exception {
        //Arrange: Setup fake service return value
        String survivorName = "Rick";
        String expectedSurvivorName = "Rick";
        int expectedSurvivorCount = 1;
        SurvivorType survivorType = SurvivorType.OUTLAW;
        Survivor rick = new Survivor(survivorName, survivorType);
        Long responseId = null;
        String responseType = "OUTLAW";
        List<Skill> responseSkills = rick.getSkills();
        List<ItemResponse> responseGear = List.of();
        SurvivorResponse rickResponse = new SurvivorResponse(responseId, survivorName, responseType, responseSkills, responseGear);
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
        String survivorName = "Jessica";
        String expectedSurvivorName = "Jessica";
        SurvivorType survivorType = SurvivorType.HERO;
        Survivor jessica = new Survivor(survivorName, survivorType);
        Long responseId = 1L;
        String responseType = "HERO";
        List<Skill> responseSkills = jessica.getSkills();
        List<ItemResponse> responseGear = List.of();
        SurvivorResponse jessicaResponse = new SurvivorResponse(responseId, survivorName, responseType, responseSkills, responseGear);
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
        String survivorName = "Alice";
        Long expectedSurvivorId = 1L;
        String expectedSurvivorType = "TESTSURVIVOR";
        SurvivorType survivorType = SurvivorType.TESTSURVIVOR;
        Survivor survivor = new Survivor(survivorName, survivorType);
        survivor.setId(expectedSurvivorId);
        SurvivorResponse response = response(expectedSurvivorId, survivorName, expectedSurvivorType);
        when(survivorService.create(KEYCLOAK_ID, "Alice", SurvivorType.TESTSURVIVOR)).thenReturn(survivor);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(KEYCLOAK_ID)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        mockMvc.perform(post("/api/survivors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"type\":\"testsurvivor\"}"))
                .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(expectedSurvivorId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(survivorName))
            .andExpect(jsonPath("$.type").value(expectedSurvivorType));

        verify(survivorService).create(KEYCLOAK_ID, "Alice", SurvivorType.TESTSURVIVOR);
    }

    @Test
    void addSkill_ReturnsUpdatedSurvivor() throws Exception {
        String survivorName = "Alice";
        String expectedSurvivorName = "Alice";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
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
        String survivorName = "Alice";
        String expectedSurvivorName = "Alice";
        SurvivorType survivorType = SurvivorType.CAREGIVER;
        Survivor survivor = new Survivor(survivorName, survivorType);
        String itemName = "Medkit";
        Double itemWeight = 2.5;
        Double itemDurability = 10.0;
        Tool medkit = new Tool(itemName, itemWeight, itemDurability);
        String itemType = "tool";
        Double itemDamage = null;
        ItemLoadRequest request = new ItemLoadRequest(itemType, itemName, itemWeight, itemDurability, itemDamage);
        when(survivorService.findById(1L)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"tool\",\"name\":\"Medkit\",\"weight\":2.5,\"durability\":10}"))
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

    private SurvivorResponse response(Long id, String name, String type) {
        return new SurvivorResponse(id, name, type, List.of(), List.of());
    }
}
