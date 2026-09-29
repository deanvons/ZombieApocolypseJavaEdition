package controllers;


import no.loopacademy.controllers.SurvivorController;
import no.loopacademy.Main;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;
import no.loopacademy.services.SurvivorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(SurvivorController.class)
@ContextConfiguration(classes = Main.class)
@AutoConfigureMockMvc(addFilters = false)
public class SurvivorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurvivorService survivorService;

    @Test
    void getSurvivor_ReturnsSurvivor_WhenFound() throws Exception {
        // Arrange
        Survivor survivor = new Survivor("Alice", SurvivorType.CAREGIVER);
        survivor.setId(1L);
        when(survivorService.findById(1L)).thenReturn(survivor);

        // Act & Assert
        mockMvc.perform(get("/api/survivors/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Alice"));
    }
}

