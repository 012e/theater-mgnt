// Test for CinemaServiceImpl
package com.theatermgnt.theatermgnt.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.theatermgnt.theatermgnt.authorization.cinema.service.CinemaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.authorization.cinema.dto.request.CinemaCreationRequest;
import com.theatermgnt.theatermgnt.authorization.cinema.dto.request.CinemaUpdateRequest;
import com.theatermgnt.theatermgnt.authorization.cinema.dto.response.CinemaResponse;
import com.theatermgnt.theatermgnt.authorization.cinema.entity.Cinema;
import com.theatermgnt.theatermgnt.authorization.cinema.mapper.CinemaMapper;
import com.theatermgnt.theatermgnt.authorization.cinema.repository.CinemaRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
public class CinemaServiceImplTest {

    @Mock
    CinemaRepository cinemaRepository;

    @Mock
    CinemaMapper cinemaMapper;

    @InjectMocks
    CinemaServiceImpl cinemaService;

    private Cinema sampleCinema() {
        Cinema c = new Cinema();
        // set fields via reflection or setters if available; assume setters exist
        c.setId("cinema-1");
        c.setName("Sample Cinema");
        c.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        return c;
    }

    private CinemaResponse sampleResponse() {
        CinemaResponse r = new CinemaResponse();
        r.setId("cinema-1");
        r.setName("Sample Cinema");
        return r;
    }

    @Test
    public void createCinema_success() {
        CinemaCreationRequest req = new CinemaCreationRequest();
        req.setName("Sample Cinema");

        Cinema cinema = sampleCinema();
        CinemaResponse expected = sampleResponse();

        when(cinemaRepository.existsByName(req.getName())).thenReturn(false);
        when(cinemaMapper.toCinemas(req)).thenReturn(cinema);
        when(cinemaRepository.save(cinema)).thenReturn(cinema);
        when(cinemaMapper.toCinemaResponse(cinema)).thenReturn(expected);

        CinemaResponse actual = cinemaService.createCinema(req);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());

        verify(cinemaRepository, times(1)).existsByName(req.getName());
        verify(cinemaMapper, times(1)).toCinemas(req);
        verify(cinemaRepository, times(1)).save(cinema);
        verify(cinemaMapper, times(1)).toCinemaResponse(cinema);
    }

    @Test
    public void createCinema_alreadyExists_throwsAppException() {
        CinemaCreationRequest req = new CinemaCreationRequest();
        req.setName("Existing Cinema");

        when(cinemaRepository.existsByName(req.getName())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> cinemaService.createCinema(req));
        assertEquals(ErrorCode.CINEMA_EXISTED, ex.getErrorCode());

        verify(cinemaRepository, times(1)).existsByName(req.getName());
        verify(cinemaMapper, never()).toCinemas(any());
    }

    @Test
    public void createCinema_repositoryThrows_propagates() {
        CinemaCreationRequest req = new CinemaCreationRequest();
        req.setName("Sample Cinema");

        Cinema cinema = sampleCinema();
        when(cinemaRepository.existsByName(req.getName())).thenReturn(false);
        when(cinemaMapper.toCinemas(req)).thenReturn(cinema);
        when(cinemaRepository.save(cinema)).thenThrow(new RuntimeException("db error"));

        assertThrows(RuntimeException.class, () -> cinemaService.createCinema(req));

        verify(cinemaRepository, times(1)).existsByName(req.getName());
        verify(cinemaRepository, times(1)).save(cinema);
    }

    @Test
    public void getCinemas_returnsMappedList() {
        Cinema c1 = sampleCinema();
        Cinema c2 = new Cinema();
        c2.setId("cinema-2");
        c2.setName("C2");

        CinemaResponse r1 = sampleResponse();
        CinemaResponse r2 = new CinemaResponse();
        r2.setId("cinema-2");
        r2.setName("C2");

        when(cinemaRepository.findAll()).thenReturn(List.of(c1, c2));
        when(cinemaMapper.toCinemaResponse(c1)).thenReturn(r1);
        when(cinemaMapper.toCinemaResponse(c2)).thenReturn(r2);

        var results = cinemaService.getCinemas();

        assertEquals(2, results.size());
        assertEquals("cinema-1", results.get(0).getId());
        assertEquals("cinema-2", results.get(1).getId());

        verify(cinemaRepository, times(1)).findAll();
        verify(cinemaMapper, times(1)).toCinemaResponse(c1);
        verify(cinemaMapper, times(1)).toCinemaResponse(c2);
    }

    @Test
    public void getCinema_found_returnsMapped() {
        String id = "cinema-1";
        Cinema cinema = sampleCinema();
        CinemaResponse resp = sampleResponse();

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(cinema));
        when(cinemaMapper.toCinemaResponse(cinema)).thenReturn(resp);

        CinemaResponse actual = cinemaService.getCinema(id);

        assertEquals(resp.getId(), actual.getId());
        assertEquals(resp.getName(), actual.getName());

        verify(cinemaRepository, times(1)).findById(id);
        verify(cinemaMapper, times(1)).toCinemaResponse(cinema);
    }

    @Test
    public void getCinema_notFound_throwsAppException() {
        String id = "not-exist";
        when(cinemaRepository.findById(id)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> cinemaService.getCinema(id));
        assertEquals(ErrorCode.CINEMA_NOT_EXISTED, ex.getErrorCode());

        verify(cinemaRepository, times(1)).findById(id);
    }

    @Test
    public void deleteCinema_invokesRepository() {
        String id = "cinema-1";

        cinemaService.deleteCinema(id);

        verify(cinemaRepository, times(1)).deleteById(id);
    }

    @Test
    public void updateCinema_success() {
        String id = "cinema-1";
        Cinema cinema = sampleCinema();
        CinemaUpdateRequest req = new CinemaUpdateRequest();
        req.setName("Updated Name");

        Cinema saved = new Cinema();
        saved.setId(id);
        saved.setName("Updated Name");

        CinemaResponse resp = new CinemaResponse();
        resp.setId(id);
        resp.setName("Updated Name");

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(cinema));
        // mapper.updateCinema is void - we verify it was called
        when(cinemaRepository.save(cinema)).thenReturn(saved);
        when(cinemaMapper.toCinemaResponse(saved)).thenReturn(resp);

        CinemaResponse actual = cinemaService.updateCinema(id, req);

        assertEquals(resp.getId(), actual.getId());
        assertEquals(resp.getName(), actual.getName());

        verify(cinemaRepository, times(1)).findById(id);
        verify(cinemaMapper, times(1)).updateCinema(cinema, req);
        verify(cinemaRepository, times(1)).save(cinema);
        verify(cinemaMapper, times(1)).toCinemaResponse(saved);
    }

    @Test
    public void updateCinema_notFound_throwsAppException() {
        String id = "nope";
        CinemaUpdateRequest req = new CinemaUpdateRequest();
        when(cinemaRepository.findById(id)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> cinemaService.updateCinema(id, req));
        assertEquals(ErrorCode.CINEMA_NOT_EXISTED, ex.getErrorCode());

        verify(cinemaRepository, times(1)).findById(id);
        verify(cinemaMapper, never()).updateCinema(any(), any());
    }

    @Test
    public void updateCinema_saveThrows_propagates() {
        String id = "cinema-1";
        Cinema cinema = sampleCinema();
        CinemaUpdateRequest req = new CinemaUpdateRequest();

        when(cinemaRepository.findById(id)).thenReturn(Optional.of(cinema));
        doThrow(new RuntimeException("db fail")).when(cinemaRepository).save(cinema);

        assertThrows(RuntimeException.class, () -> cinemaService.updateCinema(id, req));

        verify(cinemaRepository, times(1)).findById(id);
        verify(cinemaRepository, times(1)).save(cinema);
    }
}
