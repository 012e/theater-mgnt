package com.theatermgnt.theatermgnt.movie.service;

import java.util.List;

import com.theatermgnt.theatermgnt.common.enums.MovieStatus;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.request.UpdateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieResponse;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieSimpleResponse;

public interface MovieService {
    // ========== CREATE ==========
    MovieResponse createMovie(CreateMovieRequest request);

    // ========== READ ==========
    List<MovieSimpleResponse> getAllMovies();

    MovieResponse getMovieById(String id);

    List<MovieSimpleResponse> getMoviesByStatus(MovieStatus status);

    List<MovieSimpleResponse> getNowShowingMovies();

    List<MovieSimpleResponse> getComingSoonMovies();

    List<MovieSimpleResponse> searchMoviesByTitle(String title);

    List<MovieSimpleResponse> getMoviesByGenre(String genreId);

    // ========== UPDATE ==========
    MovieResponse updateMovie(String id, UpdateMovieRequest request);

    MovieResponse archiveMovie(String id);

    // ========== DELETE ==========
    void deleteMovie(String movieId);
}
