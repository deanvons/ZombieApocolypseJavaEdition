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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;
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
    private static final String KEYCLOAK_ID = "test-user";
    private String survivorName;
    private String expectedSurvivorName;
    private SurvivorType survivorType;
    private String expectedSurvivorTypeString;
    private Long survivorId;
    private Long actionId;

    private String itemMedkit;
    private Double itemMedkitWeight;
    private Double itemMedkitDurability;

    @Autowired 
    private MockMvc mockMvc;                //sends fake HTTP requests

    @MockitoBean 
    private SurvivorService survivorService; //fake service, w/o db


    @BeforeEach
    void setup(){
        survivorName = "Rick";
        expectedSurvivorName = survivorName;
        survivorType = SurvivorType.OUTLAW;
        expectedSurvivorTypeString = survivorType.name();
        survivorId = 1L;
        actionId = 2L;

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
            .andExpect(jsonPath("$[0].name").value(expectedSurvivorName))
            .andExpect(jsonPath("$[0].type").value(expectedSurvivorTypeString));
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
        when(survivorService.findById(survivorId)).thenReturn(jessica);

        //Act + assert
        mockMvc.perform(get("/api/survivors/" + survivorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(expectedSurvivorName))
            .andExpect(jsonPath("$.type").value(expectedSurvivorTypeString));
    }

    @Test
    void getSurvivorByIdShouldReturn404IfNotFound() throws Exception {
        //Arrange
        int expectedStatusCode = 404;
        String expectedErrorMessage = "Survivor Not Found";
        when(survivorService.findById(survivorId))
            .thenThrow(new SurvivorNotFoundException(expectedErrorMessage));

        //Act + assert
        mockMvc.perform(get("/api/survivors/" + survivorId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(expectedStatusCode))
            .andExpect(jsonPath("$.message").value(expectedErrorMessage));
        
    }

    /** POST, PUT, DELETE requests */
    @Test
    void createSurvivor_ReturnsCreatedSurvivor() throws Exception {
        Survivor survivor = new Survivor(survivorName, survivorType);
        survivor.setId(survivorId);
        // SurvivorResponse response = response(expectedSurvivorId, survivorName, expectedSurvivorType);
        when(survivorService.create(KEYCLOAK_ID, survivorName, survivorType)).thenReturn(survivor);
        loginAs(KEYCLOAK_ID);

        mockMvc.perform(post("/api/survivors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + survivorName
                                + "\",\"type\":\"" + expectedSurvivorTypeString + "\"}"))
                .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(survivorId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(survivorName))
            .andExpect(jsonPath("$.type").value(expectedSurvivorTypeString));

        verify(survivorService).create(KEYCLOAK_ID, expectedSurvivorName, survivorType);
    }

    @Test
    void addSkill_ReturnsUpdatedSurvivor() throws Exception {
        Survivor survivor = new Survivor(survivorName, survivorType);
        when(survivorService.findById(survivorId)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/" + survivorId + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skill\":\"Accuracy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(expectedSurvivorName));

        verify(survivorService).addSkill(survivorId, Skill.Accuracy);
        verify(survivorService).findById(survivorId);
    }

    @Test
    void deleteSurvivorSkill_RemovesSkillFromSurvivor() throws Exception {
        mockMvc.perform(delete("/api/survivors/" + survivorId + "/skills/PsychologicalSupport"))
                .andExpect(status().isNoContent());

        verify(survivorService).removeSkill(survivorId, Skill.PsychologicalSupport);
    }

    @Test
    void loadItem_ReturnsUpdatedSurvivor() throws Exception {

        Survivor survivor = new Survivor(survivorName, survivorType);

        Tool medkit = new Tool(itemMedkit, itemMedkitWeight, itemMedkitDurability);
        when(survivorService.findById(survivorId)).thenReturn(survivor);

        mockMvc.perform(post("/api/survivors/" + survivorId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"tool\",\"name\":\"" + itemMedkit
                                + "\",\"weight\":" + itemMedkitWeight
                                + ",\"durability\":" + itemMedkitDurability + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(expectedSurvivorName));

        verify(survivorService).loadItem(survivorId, medkit);
    }

    @Test
    void performAction_ReturnsActionEffectiveness() throws Exception {
        double expectedEffectiveness = 72.5;
        ActionResult result = new ActionResult(null, null, expectedEffectiveness);
        when(survivorService.performAction(survivorId, actionId)).thenReturn(result);

        mockMvc.perform(post("/api/survivors/" + survivorId + "/actions/" + actionId))
                .andExpect(status().isOk())
            .andExpect(jsonPath("$.survivorId").value(survivorId))
            .andExpect(jsonPath("$.actionId").value(actionId))
            .andExpect(jsonPath("$.effectiveness").value(expectedEffectiveness));

        verify(survivorService).performAction(survivorId, actionId);
    }

    // Security filters are off in this class (addFilters = false), so jwt() from spring-security-test
    // never reaches @AuthenticationPrincipal. Put the token straight into the security context instead;
    // spring-security-test clears it after each test.
    private void loginAs(String keycloakId) {
        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject(keycloakId)
            .build();
        TestSecurityContextHolder.setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
