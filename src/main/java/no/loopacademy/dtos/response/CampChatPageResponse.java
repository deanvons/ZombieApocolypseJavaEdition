package no.loopacademy.dtos.response;

import java.util.List;

public record CampChatPageResponse(List<CampChatMessageResponse> items, String nextCursor) {
}