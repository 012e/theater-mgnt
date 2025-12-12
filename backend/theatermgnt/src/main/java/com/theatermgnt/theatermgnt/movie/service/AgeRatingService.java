package com.theatermgnt.theatermgnt.movie.service;

import java.util.List;

import com.theatermgnt.theatermgnt.movie.dto.request.CreateAgeRatingRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.AgeRatingResponse;

public interface AgeRatingService {
    // CREATE
    AgeRatingResponse createAgeRating(CreateAgeRatingRequest request);

    // READ
    List<AgeRatingResponse> getAllAgeRatings();

    AgeRatingResponse getAgeRatingById(String id);

    AgeRatingResponse getAgeRatingByCode(String code);
}
