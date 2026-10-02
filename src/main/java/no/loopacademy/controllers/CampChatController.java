package no.loopacademy.controllers;

import java.util.UUID;

import jakarta.validation.Valid;
import no.loopacademy.dtos.request.CampChatSendRequest;
import no.loopacademy.dtos.response.CampChatMessageResponse;
import no.loopacademy.dtos.response.CampChatPageResponse;
import no.loopacademy.dtos.response.CampChatVisitResponse;
import no.loopacademy.models.camp.CampChatVisit;
import no.loopacademy.services.CampChatService;
import no.loopacademy.services.CampChatStreamRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/camp")
public class CampChatController {

    private final CampChatService campChatService;
    private final CampChatStreamRegistry streamRegistry;

    public CampChatController(CampChatService campChatService, CampChatStreamRegistry streamRegistry) {
        this.campChatService = campChatService;
        this.streamRegistry = streamRegistry;
    }

    @PostMapping("/visits")
    public ResponseEntity<CampChatVisitResponse> startVisit(@AuthenticationPrincipal Jwt jwt) {
        CampChatVisitResponse visit = campChatService.startVisit(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(visit);
    }

    @GetMapping("/visits/{visitId}/messages")
    public ResponseEntity<CampChatPageResponse> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID visitId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String cursor) {
        return ResponseEntity.ok(campChatService.getMessages(jwt.getSubject(), visitId, limit, cursor));
    }

    @PostMapping("/visits/{visitId}/messages")
    public ResponseEntity<CampChatMessageResponse> sendMessage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID visitId,
            @Valid @RequestBody CampChatSendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(campChatService.sendMessage(jwt.getSubject(), visitId, request.content()));
    }

    @GetMapping(value = "/visits/{visitId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID visitId) {
        CampChatVisit visit = campChatService.getAuthorizedVisit(jwt.getSubject(), visitId);
        return streamRegistry.register(visit.getId(), visit.getCamp().getId(),
                visit.getStartedAt(), visit.getExpiresAt());
    }
}