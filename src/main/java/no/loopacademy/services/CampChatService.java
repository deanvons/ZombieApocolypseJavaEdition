package no.loopacademy.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import no.loopacademy.dtos.response.CampChatMessageResponse;
import no.loopacademy.dtos.response.CampChatPageResponse;
import no.loopacademy.dtos.response.CampChatVisitResponse;
import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.exceptions.ForbiddenException;
import no.loopacademy.exceptions.ResourceNotFoundException;
import no.loopacademy.models.camp.Camp;
import no.loopacademy.models.camp.CampChatVisit;
import no.loopacademy.models.camp.CampMessage;
import no.loopacademy.models.userprofile.UserProfile;
import no.loopacademy.repositories.CampChatVisitRepository;
import no.loopacademy.repositories.CampMessageRepository;
import no.loopacademy.repositories.CampRepository;
import no.loopacademy.repositories.UserProfileRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampChatService {

    public static final String SHARED_CAMP_ID = "shared";
    public static final int MAX_MESSAGE_CODE_POINTS = 500;
    public static final int MAX_PAGE_SIZE = 100;
    public static final long VISIT_LIFETIME_HOURS = 24;

    private final UserProfileRepository userProfileRepository;
    private final CampRepository campRepository;
    private final CampChatVisitRepository visitRepository;
    private final CampMessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CampChatService(
            UserProfileRepository userProfileRepository,
            CampRepository campRepository,
            CampChatVisitRepository visitRepository,
            CampMessageRepository messageRepository,
            ApplicationEventPublisher eventPublisher) {
        this.userProfileRepository = userProfileRepository;
        this.campRepository = campRepository;
        this.visitRepository = visitRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CampChatVisitResponse startVisit(String keycloakId) {
        UserProfile profile = userProfileRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("No profile found for this user"));
        if (!profile.hasSurvivor()) {
            throw new ForbiddenException("An owned survivor is required to enter Camp");
        }
        Camp camp = campRepository.findById(SHARED_CAMP_ID)
                .orElseThrow(() -> new IllegalStateException("The shared Camp is not configured"));
        Instant startedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        CampChatVisit visit = visitRepository.save(new CampChatVisit(
                camp, profile, profile.getSurvivor(), startedAt,
                startedAt.plus(VISIT_LIFETIME_HOURS, ChronoUnit.HOURS)));
        return new CampChatVisitResponse(visit.getId(), visit.getStartedAt());
    }

    @Transactional(readOnly = true)
    public CampChatPageResponse getMessages(String keycloakId, UUID visitId, int limit, String cursor) {
        CampChatVisit visit = getAuthorizedVisit(keycloakId, visitId);
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw new BusinessRuleException("limit must be between 1 and " + MAX_PAGE_SIZE);
        }

        CursorPosition position = decodeCursor(cursor);
        if (position == null) {
            return getLatestMessages(visit, limit);
        }
        // With a cursor (e.g. recovering after a dropped event stream): messages after it, from the visit start on
        List<CampMessage> results = messageRepository.findVisitMessages(
                visit.getCamp().getId(), visit.getStartedAt(),
                position.createdAt(), position.id(),
                PageRequest.of(0, limit + 1));
        boolean hasMore = results.size() > limit;
        List<CampChatMessageResponse> items = results.stream()
                .limit(limit)
                .map(CampChatService::toResponse)
                .toList();
        String nextCursor = hasMore ? encodeCursor(items.get(items.size() - 1)) : null;
        return new CampChatPageResponse(items, nextCursor);
    }

    // Entering Camp (no cursor): the camp's newest messages, including ones sent before this visit,
    // so players see what was said while they were away. Fetched newest-first, then reversed to
    // chronological order. nextCursor is null since nothing is newer; older messages are not paged.
    private CampChatPageResponse getLatestMessages(CampChatVisit visit, int limit) {
        List<CampChatMessageResponse> items = messageRepository
                .findLatestMessages(visit.getCamp().getId(), PageRequest.of(0, limit))
                .reversed()
                .stream()
                .map(CampChatService::toResponse)
                .toList();
        return new CampChatPageResponse(items, null);
    }

    @Transactional
    public CampChatMessageResponse sendMessage(String keycloakId, UUID visitId, String requestedContent) {
        CampChatVisit visit = getAuthorizedVisit(keycloakId, visitId);
        String content = validateContent(requestedContent);
        CampMessage message = messageRepository.saveAndFlush(new CampMessage(
            visit.getCamp(), visit.getSurvivor(), content, Instant.now().truncatedTo(ChronoUnit.MICROS)));
        CampChatMessageResponse response = toResponse(message);
        eventPublisher.publishEvent(new CampMessageCommittedEvent(visit.getCamp().getId(), response));
        return response;
    }

    @Transactional(readOnly = true)
    public CampChatVisit getAuthorizedVisit(String keycloakId, UUID visitId) {
        return visitRepository.findByIdAndOwner_KeycloakIdAndExpiresAtAfter(visitId, keycloakId, Instant.now())
                .orElseThrow(() -> new ResourceNotFoundException("Camp visit not found or expired"));
    }

    private static String validateContent(String requestedContent) {
        if (requestedContent == null || requestedContent.strip().isBlank()) {
            throw new BusinessRuleException("content must not be blank");
        }
        String content = requestedContent.strip();
        if (content.codePointCount(0, content.length()) > MAX_MESSAGE_CODE_POINTS) {
            throw new BusinessRuleException("content must be at most 500 Unicode code points");
        }
        return content;
    }

    private static CampChatMessageResponse toResponse(CampMessage message) {
        return new CampChatMessageResponse(
                message.getId(), message.getContent(), message.getSender().getName(), message.getCreatedAt());
    }

    public static String encodeCursor(CampChatMessageResponse message) {
        String value = message.createdAt() + "|" + message.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static CursorPosition decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException();
            }
            return new CursorPosition(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("cursor is invalid");
        }
    }

    private record CursorPosition(Instant createdAt, UUID id) {
    }
}