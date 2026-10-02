package no.loopacademy.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import no.loopacademy.config.authConfig;
import no.loopacademy.dtos.response.CampChatVisitResponse;
import no.loopacademy.services.CampChatService;
import no.loopacademy.services.CampChatStreamRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CampChatController.class)
@Import(authConfig.class)
@SuppressWarnings("null")
class CampChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampChatService campChatService;

    @MockitoBean
    private CampChatStreamRegistry streamRegistry;

    @Test
    void visitCreationRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/camp/visits"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void visitCreationUsesAuthenticatedSubject() throws Exception {
        UUID visitId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2026-10-02T16:30:00Z");
        when(campChatService.startVisit("keycloak-subject"))
                .thenReturn(new CampChatVisitResponse(visitId, startedAt));

        mockMvc.perform(post("/api/camp/visits")
                        .with(jwt().jwt(token -> token.subject("keycloak-subject"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visitId").value(visitId.toString()))
                .andExpect(jsonPath("$.startedAt").value("2026-10-02T16:30:00Z"));

        verify(campChatService).startVisit("keycloak-subject");
    }
}