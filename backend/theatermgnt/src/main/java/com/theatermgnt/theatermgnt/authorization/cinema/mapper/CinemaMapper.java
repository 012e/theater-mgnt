package com.theatermgnt.theatermgnt.authorization.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.theatermgnt.theatermgnt.authorization.cinema.dto.request.CinemaCreationRequest;
import com.theatermgnt.theatermgnt.authorization.cinema.dto.request.CinemaUpdateRequest;
import com.theatermgnt.theatermgnt.authorization.cinema.dto.response.CinemaResponse;
import com.theatermgnt.theatermgnt.authorization.cinema.entity.Cinema;

@Mapper(componentModel = "spring")
public interface CinemaMapper {
    Cinema toCinemas(CinemaCreationRequest request);

    CinemaResponse toCinemaResponse(Cinema cinema);

    void updateCinema(@MappingTarget Cinema cinema, CinemaUpdateRequest request);
}
