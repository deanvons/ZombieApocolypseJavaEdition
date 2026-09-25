package no.loopacademy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public class SurvivorMapper {
    SurvivorResponse toResponse(Survivor survivor);
    
}
