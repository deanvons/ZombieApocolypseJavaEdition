package no.loopacademy.controllers;

import static org.junit.jupiter.api.Assertions.fail;
import no.loopacademy.config.authConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// @WebMvcTest doesn't load @Configuration classes, so without this import the test
// runs with Spring Boot's default security (everything locked, 401) instead of ours.
@WebMvcTest(HealthCheckController.class)
@Import(authConfig.class)
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void getApiHealthShouldReturnHtmlResponseOk() {
        try {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/api/health"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail();
        }
    }
}
