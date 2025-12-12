package com.theatermgnt.theatermgnt.movie.service;

import java.util.List;

import com.theatermgnt.theatermgnt.movie.dto.request.CreateGenreRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.GenreResponse;

public interface GenreService {
    // CREATE
    GenreResponse createGenre(CreateGenreRequest request);

    // READ
    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(String id);

    GenreResponse getGenreByName(String name);
}
