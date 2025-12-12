package com.theatermgnt.theatermgnt.cinema.service;

import java.util.List;

import com.theatermgnt.theatermgnt.cinema.dto.request.CinemaCreationRequest;
import com.theatermgnt.theatermgnt.cinema.dto.request.CinemaUpdateRequest;
import com.theatermgnt.theatermgnt.cinema.dto.response.CinemaResponse;

public interface CinemaService {
    CinemaResponse createCinema(CinemaCreationRequest request);

    List<CinemaResponse> getCinemas();

    CinemaResponse getCinema(String cinemaId);

    void deleteCinema(String cinemaId);

    CinemaResponse updateCinema(String cinemaId, CinemaUpdateRequest request);
}
