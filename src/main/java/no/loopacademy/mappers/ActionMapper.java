package no.loopacademy.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import no.loopacademy.dtos.response.ActionResponse;
import no.loopacademy.models.actions.Action;

@Mapper(componentModel = "spring")
public interface ActionMapper {

    ActionResponse toResponse(Action action);

    List<ActionResponse> toResponse(List<Action> actions);
}
