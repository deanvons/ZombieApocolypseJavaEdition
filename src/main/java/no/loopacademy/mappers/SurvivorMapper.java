package no.loopacademy.mappers;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.mapstruct.Mapper;

import no.loopacademy.dtos.response.SurvivorResponse;
import no.loopacademy.exceptions.BusinessRuleException;
import no.loopacademy.models.survivors.Survivor;
import no.loopacademy.models.survivors.SurvivorType;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface SurvivorMapper {

    SurvivorResponse toResponse(Survivor survivor);

    List<SurvivorResponse> toResponse(List<Survivor> survivors);

    // Case-insensitive
    default SurvivorType toSurvivorType(String type) {
        try {
            return SurvivorType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessRuleException("Unknown survivor type: " + type + ". Valid types are: " + Arrays.toString(SurvivorType.values()));
        }
    }
    
}
