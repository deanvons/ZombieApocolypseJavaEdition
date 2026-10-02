package no.loopacademy.services;

import no.loopacademy.dtos.response.CampChatMessageResponse;

public record CampMessageCommittedEvent(String campId, CampChatMessageResponse message) {
}