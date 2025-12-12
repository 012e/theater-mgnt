package com.theatermgnt.theatermgnt.movie.service;

import com.theatermgnt.theatermgnt.movie.dto.request.CreateGenreRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.GenreResponse;

import java.util.List;

public interface GenreService {
    // CREATE
    GenreResponse createGenre(CreateGenreRequest request);

    // READ
    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(String id);

    GenreResponse getGenreByName(String name);
}
