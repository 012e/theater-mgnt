package com.theatermgnt.theatermgnt.movie.service;

import com.theatermgnt.theatermgnt.movie.dto.request.CreateAgeRatingRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.AgeRatingResponse;

import java.util.List;

public interface AgeRatingService {
    // CREATE
    AgeRatingResponse createAgeRating(CreateAgeRatingRequest request);

    // READ
    List<AgeRatingResponse> getAllAgeRatings();

    AgeRatingResponse getAgeRatingById(String id);

    AgeRatingResponse getAgeRatingByCode(String code);
}
