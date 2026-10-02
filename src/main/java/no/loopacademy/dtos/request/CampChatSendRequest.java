package no.loopacademy.dtos.request;

import jakarta.validation.constraints.NotNull;

public record CampChatSendRequest(@NotNull String content) {
}