package com.theatermgnt.theatermgnt.movie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.theatermgnt.theatermgnt.common.enums.MovieStatus;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.request.UpdateMovieRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieResponse;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieSimpleResponse;
import com.theatermgnt.theatermgnt.movie.entity.AgeRating;
import com.theatermgnt.theatermgnt.movie.entity.Genre;
import com.theatermgnt.theatermgnt.movie.entity.Movie;
import com.theatermgnt.theatermgnt.movie.mapper.MovieMapper;
import com.theatermgnt.theatermgnt.movie.repository.AgeRatingRepository;
import com.theatermgnt.theatermgnt.movie.repository.GenreRepository;
import com.theatermgnt.theatermgnt.movie.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MovieServiceImplTest {

    @Mock
    MovieRepository movieRepository;

    @Mock
    AgeRatingRepository ageRatingRepository;

    @Mock
    GenreRepository genreRepository;

    @Mock
    MovieMapper movieMapper;

    @InjectMocks
    MovieServiceImpl movieService;

    @BeforeEach
    void setUp() {
        // MockitoExtension + @InjectMocks will initialize
    }

    // Helper builders
    private CreateMovieRequest createCreateRequest(String ageRatingId, String... genreIds) {
        CreateMovieRequest r = mock(CreateMovieRequest.class);
        when(r.getAgeRatingId()).thenReturn(ageRatingId);
        Set<String> genreSet = new HashSet<>(Arrays.asList(genreIds));
        when(r.getGenreIds()).thenReturn(genreSet);
        when(r.getTitle()).thenReturn("Test Title");
        return r;
    }

    private UpdateMovieRequest createUpdateRequest(String ageRatingId, Set<String> genreIds) {
        UpdateMovieRequest r = mock(UpdateMovieRequest.class);
        when(r.getAgeRatingId()).thenReturn(ageRatingId);
        when(r.getGenreIds()).thenReturn(genreIds);
        when(r.getTitle()).thenReturn("Updated Title");
        return r;
    }

    // ========== CREATE ==========
    @Test
    void createMovie_success() {
        String ageId = "age-1";
        String g1 = "g1";
        String g2 = "g2";

        CreateMovieRequest req = createCreateRequest(ageId, g1, g2);

        AgeRating ageRating = new AgeRating();
        ageRating.setId(ageId);

        Genre genre1 = new Genre(); genre1.setId(g1);
        Genre genre2 = new Genre(); genre2.setId(g2);

        Movie mappedMovie = new Movie();
        mappedMovie.setTitle("Test Title");

        Movie savedMovie = new Movie();
        savedMovie.setId("movie-1");
        savedMovie.setTitle("Test Title");
        savedMovie.setAgeRating(ageRating);
        savedMovie.setGenres(new HashSet<>(Arrays.asList(genre1, genre2)));

        MovieResponse expectedResponse = mock(MovieResponse.class);

        when(ageRatingRepository.findById(ageId)).thenReturn(Optional.of(ageRating));
        when(genreRepository.findById(g1)).thenReturn(Optional.of(genre1));
        when(genreRepository.findById(g2)).thenReturn(Optional.of(genre2));
        when(movieMapper.toMovie(req)).thenReturn(mappedMovie);
        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);
        when(movieMapper.toMovieResponse(savedMovie)).thenReturn(expectedResponse);

        MovieResponse res = movieService.createMovie(req);

        assertSame(expectedResponse, res);

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(captor.capture());
        Movie argSaved = captor.getValue();
        assertNotNull(argSaved.getId());
        assertEquals(ageRating, argSaved.getAgeRating());
        assertEquals(2, argSaved.getGenres().size());
    }

    @Test
    void createMovie_ageRatingNotFound_throws() {
        CreateMovieRequest req = createCreateRequest("missing-age", "g1");
        when(ageRatingRepository.findById("missing-age")).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> movieService.createMovie(req));
    }

    @Test
    void createMovie_genreNotFound_throws() {
        String ageId = "age-1";
        CreateMovieRequest req = createCreateRequest(ageId, "g1", "missing");
        when(ageRatingRepository.findById(ageId)).thenReturn(Optional.of(new AgeRating()));
        when(genreRepository.findById("g1")).thenReturn(Optional.of(new Genre()));
        when(genreRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> movieService.createMovie(req));
    }

    // ========== READ ==========
    @Test
    void getAllMovies_returnsMappedList() {
        Movie m1 = new Movie(); m1.setId("m1");
        Movie m2 = new Movie(); m2.setId("m2");

        when(movieRepository.findAllWithGenres()).thenReturn(Arrays.asList(m1, m2));

        MovieSimpleResponse s1 = mock(MovieSimpleResponse.class);
        MovieSimpleResponse s2 = mock(MovieSimpleResponse.class);
        when(movieMapper.toMovieSimpleResponse(m1)).thenReturn(s1);
        when(movieMapper.toMovieSimpleResponse(m2)).thenReturn(s2);

        List<MovieSimpleResponse> res = movieService.getAllMovies();
        assertEquals(2, res.size());
        assertTrue(res.containsAll(Arrays.asList(s1, s2)));
    }

    @Test
    void getMovieById_exists_returnsMapped() {
        Movie m = new Movie(); m.setId("mid");
        when(movieRepository.findById("mid")).thenReturn(Optional.of(m));
        MovieResponse r = mock(MovieResponse.class);
        when(movieMapper.toMovieResponse(m)).thenReturn(r);

        MovieResponse out = movieService.getMovieById("mid");
        assertSame(r, out);
    }

    @Test
    void getMovieById_missing_throws() {
        when(movieRepository.findById("x")).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> movieService.getMovieById("x"));
    }

    @Test
    void getMoviesByStatus_maps() {
        Movie m = new Movie(); m.setId("m");
        when(movieRepository.findByStatus(MovieStatus.now_showing)).thenReturn(List.of(m));
        MovieSimpleResponse s = mock(MovieSimpleResponse.class);
        when(movieMapper.toMovieSimpleResponse(m)).thenReturn(s);

        List<MovieSimpleResponse> res = movieService.getMoviesByStatus(MovieStatus.now_showing);
        assertEquals(1, res.size());
        assertSame(s, res.getFirst());
    }

    @Test
    void getNowShowingMovies_and_ComingSoonMovies_delegateToRepo() {
        Movie now = new Movie(); now.setId("n");
        Movie coming = new Movie(); coming.setId("c");
        when(movieRepository.findNowShowingMovies(MovieStatus.now_showing)).thenReturn(List.of(now));
        when(movieRepository.findComingSoonMovies(MovieStatus.coming_soon)).thenReturn(List.of(coming));
        when(movieMapper.toMovieSimpleResponse(now)).thenReturn(mock(MovieSimpleResponse.class));
        when(movieMapper.toMovieSimpleResponse(coming)).thenReturn(mock(MovieSimpleResponse.class));

        assertEquals(1, movieService.getNowShowingMovies().size());
        assertEquals(1, movieService.getComingSoonMovies().size());
    }

    @Test
    void searchMoviesByTitle_delegates() {
        Movie m = new Movie(); m.setId("s");
        when(movieRepository.findByTitleContainingIgnoreCase("abc")).thenReturn(List.of(m));
        when(movieMapper.toMovieSimpleResponse(m)).thenReturn(mock(MovieSimpleResponse.class));
        List<MovieSimpleResponse> res = movieService.searchMoviesByTitle("abc");
        assertEquals(1, res.size());
    }

    @Test
    void getMoviesByGenre_genreNotExists_throws() {
        when(genreRepository.existsById("g")).thenReturn(false);
        assertThrows(AppException.class, () -> movieService.getMoviesByGenre("g"));
    }

    @Test
    void getMoviesByGenre_success() {
        when(genreRepository.existsById("g")).thenReturn(true);
        Movie m = new Movie(); m.setId("mg");
        when(movieRepository.findByGenreId("g")).thenReturn(List.of(m));
        when(movieMapper.toMovieSimpleResponse(m)).thenReturn(mock(MovieSimpleResponse.class));
        List<MovieSimpleResponse> res = movieService.getMoviesByGenre("g");
        assertEquals(1, res.size());
    }

    // ========== UPDATE ==========
    @Test
    void updateMovie_notFound_throws() {
        UpdateMovieRequest req = createUpdateRequest(null, null);
        when(movieRepository.findById("no"))
            .thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> movieService.updateMovie("no", req));
    }

    @Test
    void updateMovie_updatesAgeRatingAndGenres_whenProvided() {
        Movie existing = new Movie(); existing.setId("e");
        when(movieRepository.findById("e")).thenReturn(Optional.of(existing));

        // Prepare request with ageRating and genres
        String newAgeId = "ageNew";
        String g1 = "g1";
        String g2 = "g2";
        UpdateMovieRequest req = createUpdateRequest(newAgeId, new HashSet<>(Arrays.asList(g1, g2)));

        AgeRating newAge = new AgeRating(); newAge.setId(newAgeId);
        Genre gen1 = new Genre(); gen1.setId(g1);
        Genre gen2 = new Genre(); gen2.setId(g2);

        when(ageRatingRepository.findById(newAgeId)).thenReturn(Optional.of(newAge));
        when(genreRepository.findById(g1)).thenReturn(Optional.of(gen1));
        when(genreRepository.findById(g2)).thenReturn(Optional.of(gen2));

        // movieMapper.updateMovieFromRequest is void - just verify it's called
        doNothing().when(movieMapper).updateMovieFromRequest(eq(req), eq(existing));

        Movie saved = new Movie(); saved.setId("e");
        when(movieRepository.save(existing)).thenReturn(saved);
        when(movieMapper.toMovieResponse(saved)).thenReturn(mock(MovieResponse.class));

        movieService.updateMovie("e", req);

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(captor.capture());
        Movie persisted = captor.getValue();

        assertEquals(newAge, persisted.getAgeRating());
        assertEquals(2, persisted.getGenres().size());
        verify(movieMapper).updateMovieFromRequest(eq(req), eq(existing));
    }

    @Test
    void archiveMovie_setsStatusArchived() {
        Movie m = new Movie(); m.setId("a");
        when(movieRepository.findById("a")).thenReturn(Optional.of(m));
        Movie saved = new Movie(); saved.setId("a"); saved.setStatus(MovieStatus.archived);
        when(movieRepository.save(m)).thenReturn(saved);
        when(movieMapper.toMovieResponse(saved)).thenReturn(mock(MovieResponse.class));

        movieService.archiveMovie("a");
        verify(movieRepository).save(m);
        assertEquals(MovieStatus.archived, m.getStatus());
    }

    // ========== DELETE ==========
    @Test
    void deleteMovie_notFound_throws() {
        when(movieRepository.findById("no")).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> movieService.deleteMovie("no"));
    }

    @Test
    void deleteMovie_success_deletes() {
        Movie m = new Movie(); m.setId("del");
        when(movieRepository.findById("del")).thenReturn(Optional.of(m));
        doNothing().when(movieRepository).delete(m);

        movieService.deleteMovie("del");

        verify(movieRepository).delete(m);
    }
}
