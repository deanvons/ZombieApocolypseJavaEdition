package no.loopacademy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.loopacademy.dtos.response.CampChatMessageResponse;
import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.exceptions.ForbiddenException;
import no.loopacademy.exceptions.ResourceNotFoundException;
import no.loopacademy.models.camp.Camp;
import no.loopacademy.models.camp.CampChatVisit;
import no.loopacademy.models.camp.CampMessage;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.repositories.CampChatVisitRepository;
import no.loopacademy.repositories.CampMessageRepository;
import no.loopacademy.repositories.CampRepository;
import no.loopacademy.repositories.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

@SuppressWarnings("null")
class CampChatServiceTest {

    private static final String KEYCLOAK_ID = "user-1";
    private static final UUID VISIT_ID = UUID.randomUUID();
    private static final Instant STARTED_AT = Instant.parse("2026-10-02T12:00:00Z");

    private UserProfileRepository profileRepository;
    private CampRepository campRepository;
    private CampChatVisitRepository visitRepository;
    private CampMessageRepository messageRepository;
    private org.springframework.context.ApplicationEventPublisher eventPublisher;
    private CampChatService service;
    private UserProfile profile;
    private Survivor survivor;
    private Camp camp;
    private CampChatVisit visit;

    @BeforeEach
    void setUp() {
        profileRepository = org.mockito.Mockito.mock(UserProfileRepository.class);
        campRepository = org.mockito.Mockito.mock(CampRepository.class);
        visitRepository = org.mockito.Mockito.mock(CampChatVisitRepository.class);
        messageRepository = org.mockito.Mockito.mock(CampMessageRepository.class);
        eventPublisher = org.mockito.Mockito.mock(org.springframework.context.ApplicationEventPublisher.class);
        service = new CampChatService(profileRepository, campRepository, visitRepository,
                messageRepository, eventPublisher);

        profile = new UserProfile(KEYCLOAK_ID, "Profile name");
        survivor = org.mockito.Mockito.mock(Survivor.class);
        when(survivor.getName()).thenReturn("Mara");
        profile.setSurvivor(survivor);
        camp = new Camp(CampChatService.SHARED_CAMP_ID, "Shared Camp");
        visit = new CampChatVisit(camp, profile, survivor, STARTED_AT, STARTED_AT.plusSeconds(86_400));
        when(visitRepository.findByIdAndOwner_KeycloakIdAndExpiresAtAfter(
                any(UUID.class), org.mockito.ArgumentMatchers.eq(KEYCLOAK_ID), any(Instant.class)))
                .thenReturn(Optional.of(visit));
    }

    @Test
    void startVisitUsesOwnedSurvivorAndServerTime() {
        when(profileRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(profile));
        when(campRepository.findById(CampChatService.SHARED_CAMP_ID)).thenReturn(Optional.of(camp));
        when(visitRepository.save(any(CampChatVisit.class))).thenAnswer(call -> call.getArgument(0));
        Instant beforeRequest = Instant.now();

        var response = service.startVisit(KEYCLOAK_ID);

        assertNotNull(response.visitId());
        assertTrue(!response.startedAt().isBefore(beforeRequest));
        verify(visitRepository).save(any(CampChatVisit.class));
    }

    @Test
    void startVisitRejectsProfileWithoutOwnedSurvivor() {
        profile.setSurvivor(null);
        when(profileRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(profile));

        assertThrows(ForbiddenException.class, () -> service.startVisit(KEYCLOAK_ID));
        verify(visitRepository, never()).save(any(CampChatVisit.class));
    }

    @Test
    void sendTrimsPlainTextAndUsesTheVisitSurvivor() {
        when(messageRepository.saveAndFlush(any(CampMessage.class))).thenAnswer(call -> call.getArgument(0));
        String content = "  <b>east gate</b> & safe  ";

        CampChatMessageResponse response = service.sendMessage(KEYCLOAK_ID, VISIT_ID, content);

        assertEquals("<b>east gate</b> & safe", response.content());
        assertEquals("Mara", response.senderName());
        assertNotNull(response.id());
        assertTrue(!response.createdAt().isBefore(Instant.now().minusSeconds(5)));
        verify(eventPublisher).publishEvent(any(CampMessageCommittedEvent.class));
    }

    @Test
    void sendRejectsBlankAndOverLimitUnicodeContent() {
        assertThrows(BusinessRuleException.class, () -> service.sendMessage(KEYCLOAK_ID, VISIT_ID, " \n\t "));
        assertThrows(BusinessRuleException.class,
                () -> service.sendMessage(KEYCLOAK_ID, VISIT_ID, "\uD83D\uDE00".repeat(501)));
        verify(messageRepository, never()).saveAndFlush(any(CampMessage.class));
    }

    @Test
    void sendAllowsFiveHundredSupplementaryUnicodeCodePoints() {
        when(messageRepository.saveAndFlush(any(CampMessage.class))).thenAnswer(call -> call.getArgument(0));

        CampChatMessageResponse response = service.sendMessage(
                KEYCLOAK_ID, VISIT_ID, "\uD83D\uDE00".repeat(500));

        assertEquals(500, response.content().codePointCount(0, response.content().length()));
    }

    @Test
    void failedPersistenceDoesNotPublishMessage() {
        when(messageRepository.saveAndFlush(any(CampMessage.class)))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class,
                () -> service.sendMessage(KEYCLOAK_ID, VISIT_ID, "still here"));
        verify(eventPublisher, never()).publishEvent(any(CampMessageCommittedEvent.class));
    }

    @Test
    void historyUsesVisitStartAndStableCursorForTiedTimestamps() {
        Instant tiedTime = STARTED_AT.plusSeconds(1);
        CampMessage first = new CampMessage(camp, survivor, "one", tiedTime);
        CampMessage second = new CampMessage(camp, survivor, "two", tiedTime);
        CampMessage third = new CampMessage(camp, survivor, "three", tiedTime);
        when(messageRepository.findVisitMessages(
                camp.getId(), STARTED_AT, null, null, PageRequest.of(0, 3)))
                .thenReturn(List.of(first, second, third));

        var page = service.getMessages(KEYCLOAK_ID, VISIT_ID, 2, null);

        assertEquals(List.of(first.getId(), second.getId()), page.items().stream()
                .map(CampChatMessageResponse::id).toList());
        String expectedCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(
                (tiedTime + "|" + second.getId()).getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedCursor, page.nextCursor());
        verify(messageRepository).findVisitMessages(
                camp.getId(), STARTED_AT, null, null, PageRequest.of(0, 3));
    }

    @Test
    void historyRejectsForgedVisitAndMalformedCursor() {
        when(visitRepository.findByIdAndOwner_KeycloakIdAndExpiresAtAfter(
                eq(VISIT_ID), eq(KEYCLOAK_ID), any(Instant.class)))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.getMessages(KEYCLOAK_ID, VISIT_ID, 50, null));

        when(visitRepository.findByIdAndOwner_KeycloakIdAndExpiresAtAfter(
                any(UUID.class), eq(KEYCLOAK_ID), any(Instant.class)))
                .thenReturn(Optional.of(visit));
        assertThrows(BusinessRuleException.class,
                () -> service.getMessages(KEYCLOAK_ID, VISIT_ID, 50, "forged"));
    }
}