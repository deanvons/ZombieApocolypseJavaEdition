package no.loopacademy.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import no.loopacademy.dtos.response.UserProfileResponse;
import no.loopacademy.models.userprofile.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    // survivor.id is read from the lazy proxy without loading the survivor row
    @Mapping(target = "survivorId", source = "survivor.id")
    UserProfileResponse toResponse(UserProfile userProfile);
}
