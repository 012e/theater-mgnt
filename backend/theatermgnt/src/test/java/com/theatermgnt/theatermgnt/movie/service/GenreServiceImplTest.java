package com.theatermgnt.theatermgnt.movie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateGenreRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.GenreResponse;
import com.theatermgnt.theatermgnt.movie.entity.Genre;
import com.theatermgnt.theatermgnt.movie.mapper.GenreMapper;
import com.theatermgnt.theatermgnt.movie.repository.GenreRepository;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    GenreRepository genreRepository;

    @Mock
    GenreMapper genreMapper;

    @InjectMocks
    GenreServiceImpl genreService;

    // Helper factories
    private CreateGenreRequest sampleRequest() {
        return CreateGenreRequest.builder()
                .id("g1")
                .name("Action")
                .build();
    }

    private Genre sampleGenre(String id, String name) {
        Genre g = new Genre();
        g.setId(id);
        g.setName(name);
        return g;
    }

    private GenreResponse sampleResponse(String id, String name) {
        return GenreResponse.builder()
                .id(id)
                .name(name)
                .movieCount(0)
                .build();
    }

    @Test
    void createGenre_success() {
        CreateGenreRequest req = sampleRequest();
        Genre mapped = sampleGenre(req.getId(), req.getName());
        Genre saved = sampleGenre(req.getId(), req.getName());
        GenreResponse expected = sampleResponse(saved.getId(), saved.getName());

        when(genreRepository.existsById(req.getId())).thenReturn(false);
        when(genreRepository.findByName(req.getName())).thenReturn(Optional.empty());
        when(genreMapper.toGenre(req)).thenReturn(mapped);
        when(genreRepository.save(mapped)).thenReturn(saved);
        when(genreMapper.toGenreResponse(saved)).thenReturn(expected);

        GenreResponse actual = genreService.createGenre(req);

        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());

        InOrder inOrder = inOrder(genreRepository);
        inOrder.verify(genreRepository).existsById(req.getId());
        inOrder.verify(genreRepository).findByName(req.getName());
        inOrder.verify(genreRepository).save(mapped);

        verify(genreMapper).toGenre(req);
        verify(genreMapper).toGenreResponse(saved);
        verifyNoMoreInteractions(genreRepository, genreMapper);
    }

    @Test
    void createGenre_idAlreadyExists_throwsAppException() {
        CreateGenreRequest req = sampleRequest();
        when(genreRepository.existsById(req.getId())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> genreService.createGenre(req));
        assertEquals(ErrorCode.GENRE_EXISTED, ex.getErrorCode());

        verify(genreRepository).existsById(req.getId());
        verify(genreRepository, never()).findByName(anyString());
        verify(genreRepository, never()).save(any());
        verifyNoInteractions(genreMapper);
    }

    @Test
    void createGenre_nameAlreadyExists_throwsAppException() {
        CreateGenreRequest req = sampleRequest();
        when(genreRepository.existsById(req.getId())).thenReturn(false);
        when(genreRepository.findByName(req.getName())).thenReturn(Optional.of(sampleGenre("g2", req.getName())));

        AppException ex = assertThrows(AppException.class, () -> genreService.createGenre(req));
        assertEquals(ErrorCode.GENRE_NAME_EXISTED, ex.getErrorCode());

        verify(genreRepository).existsById(req.getId());
        verify(genreRepository).findByName(req.getName());
        verify(genreRepository, never()).save(any());
        verifyNoInteractions(genreMapper);
    }

    @Test
    void createGenre_saveThrows_propagatesException() {
        CreateGenreRequest req = sampleRequest();
        Genre mapped = sampleGenre(req.getId(), req.getName());

        when(genreRepository.existsById(req.getId())).thenReturn(false);
        when(genreRepository.findByName(req.getName())).thenReturn(Optional.empty());
        when(genreMapper.toGenre(req)).thenReturn(mapped);
        when(genreRepository.save(mapped)).thenThrow(new RuntimeException("db down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> genreService.createGenre(req));
        assertEquals("db down", ex.getMessage());

        verify(genreRepository).existsById(req.getId());
        verify(genreRepository).findByName(req.getName());
        verify(genreMapper).toGenre(req);
        verify(genreRepository).save(mapped);
        verifyNoMoreInteractions(genreRepository, genreMapper);
    }

    @Test
    void createGenre_nullRequest_throwsNPE_andNoInteractions() {
        assertThrows(NullPointerException.class, () -> genreService.createGenre(null));
        verifyNoInteractions(genreRepository, genreMapper);
    }

    @Test
    void getAllGenres_emptyList_returnsEmpty() {
        when(genreRepository.findAll()).thenReturn(Collections.emptyList());
        when(genreMapper.toGenreResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<GenreResponse> res = genreService.getAllGenres();
        assertNotNull(res);
        assertTrue(res.isEmpty());

        verify(genreRepository).findAll();
        verify(genreMapper).toGenreResponseList(Collections.emptyList());
        verifyNoMoreInteractions(genreRepository, genreMapper);
    }

    @Test
    void getAllGenres_nonEmpty_returnsMappedList() {
        Genre g = sampleGenre("g1", "Action");
        GenreResponse r = sampleResponse("g1", "Action");
        when(genreRepository.findAll()).thenReturn(List.of(g));
        when(genreMapper.toGenreResponseList(List.of(g))).thenReturn(List.of(r));

        List<GenreResponse> res = genreService.getAllGenres();
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(r.getId(), res.get(0).getId());

        verify(genreRepository).findAll();
        verify(genreMapper).toGenreResponseList(List.of(g));
        verifyNoMoreInteractions(genreRepository, genreMapper);
    }

    @Test
    void getGenreById_success() {
        Genre g = sampleGenre("g1", "Action");
        GenreResponse r = sampleResponse("g1", "Action");
        when(genreRepository.findById("g1")).thenReturn(Optional.of(g));
        when(genreMapper.toGenreResponse(g)).thenReturn(r);

        GenreResponse res = genreService.getGenreById("g1");
        assertNotNull(res);
        assertEquals("g1", res.getId());

        verify(genreRepository).findById("g1");
        verify(genreMapper).toGenreResponse(g);
        verifyNoMoreInteractions(genreRepository, genreMapper);
    }

    @Test
    void getGenreById_notFound_throwsAppException() {
        when(genreRepository.findById("nope")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> genreService.getGenreById("nope"));
        assertEquals(ErrorCode.GENRE_NOT_EXISTED, ex.getErrorCode());

        verify(genreRepository).findById("nope");
        verifyNoInteractions(genreMapper);
    }

    @Test
    void getGenreById_nullId_propagatesRepositoryException() {
        when(genreRepository.findById(null)).thenThrow(new IllegalArgumentException("id null"));
        assertThrows(IllegalArgumentException.class, () -> genreService.getGenreById(null));
        verify(genreRepository).findById(null);
        verifyNoInteractions(genreMapper);
    }

    @Test
    void getGenreByName_success() {
        Genre g = sampleGenre("g1", "Action");
        GenreResponse r = sampleResponse("g1", "Action");
        when(genreRepository.findByName("Action")).thenReturn(Optional.of(g));
        when(genreMapper.toGenreResponse(g)).thenReturn(r);

        GenreResponse res = genreService.getGenreByName("Action");
        assertNotNull(res);
        assertEquals("Action", res.getName());

        verify(genreRepository).findByName("Action");
        verify(genreMapper).toGenreResponse(g);
        verifyNoMoreInteractions(genreRepository, genreMapper);
    }

    @Test
    void getGenreByName_notFound_throwsAppException() {
        when(genreRepository.findByName("nope")).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> genreService.getGenreByName("nope"));
        assertEquals(ErrorCode.GENRE_NOT_EXISTED, ex.getErrorCode());

        verify(genreRepository).findByName("nope");
        verifyNoInteractions(genreMapper);
    }

    @Test
    void getGenreByName_nullName_propagatesRepositoryException() {
        when(genreRepository.findByName(null)).thenThrow(new IllegalArgumentException("name null"));
        assertThrows(IllegalArgumentException.class, () -> genreService.getGenreByName(null));
        verify(genreRepository).findByName(null);
        verifyNoInteractions(genreMapper);
    }

    @Test
    void createGenre_whenIdExists_doesNotCheckName() {
        CreateGenreRequest req = sampleRequest();
        when(genreRepository.existsById(req.getId())).thenReturn(true);
        // if service incorrectly checks name first, this would also be called. Assert it is not.

        assertThrows(AppException.class, () -> genreService.createGenre(req));
        verify(genreRepository).existsById(req.getId());
        verify(genreRepository, never()).findByName(req.getName());
        verifyNoInteractions(genreMapper);
    }

    @Test
    void getGenreById_mapperReturnsNull_serviceReturnsNull() {
        Genre g = sampleGenre("g1", "Action");
        when(genreRepository.findById("g1")).thenReturn(Optional.of(g));
        when(genreMapper.toGenreResponse(g)).thenReturn(null);

        GenreResponse res = genreService.getGenreById("g1");
        assertNull(res);

        verify(genreRepository).findById("g1");
        verify(genreMapper).toGenreResponse(g);
        verifyNoMoreInteractions(genreRepository, genreMapper);
    }
}

