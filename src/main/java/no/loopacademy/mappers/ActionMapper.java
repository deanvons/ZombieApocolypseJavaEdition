package no.loopacademy.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

import no.loopacademy.dtos.response.ActionResponse;
import no.loopacademy.models.actions.Action;

// RETURN_DEFAULT: a missing list maps to [] instead of null, so requiredItems is never null
@Mapper(componentModel = "spring", nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface ActionMapper {

    ActionResponse toResponse(Action action);

    List<ActionResponse> toResponse(List<Action> actions);
}
