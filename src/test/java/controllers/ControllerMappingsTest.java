package controllers;

import no.loopacademy.controllers.HealthCheckController;
import no.loopacademy.controllers.SurvivorController;
import no.loopacademy.services.SurvivorService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import no.loopacademy.Main;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = Main.class)
@WebMvcTest(controllers = {
        HealthCheckController.class,
        SurvivorController.class
})
class ControllerMappingsTest {

    @MockitoBean
    private SurvivorService survivorService;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void controllerMappingsStartWithoutConflicts() {
    }
}