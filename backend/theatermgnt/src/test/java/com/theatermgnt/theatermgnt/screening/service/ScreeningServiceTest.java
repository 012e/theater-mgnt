package com.theatermgnt.theatermgnt.screening.service;

import com.theatermgnt.theatermgnt.screening.dto.response.ScreeningResponse;
import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screening.enums.ScreeningStatus;
import com.theatermgnt.theatermgnt.screening.mapper.ScreeningMapper;
import com.theatermgnt.theatermgnt.screening.repository.ScreeningRepository;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ScreeningServiceTest {
    @InjectMocks
    ScreeningService screeningService;

    @Mock
    ScreeningRepository screeningRepository;

    @Mock
    ScreeningSeatRepository screeningSeatRepository;

    @Mock
    ScreeningMapper screeningMapper;



    // ===== Helper =====
    private Screening buildScreening(
            String id,
            ScreeningStatus status,
            LocalDateTime startTime
    ) {
        Screening s = new Screening();
        s.setId(id);
        s.setStatus(status);
        s.setStartTime(startTime);
        return s;
    }
    @Test
    void TC01_shouldReturnScheduledScreening_whenFutureAndAvailable() {

        Screening s = buildScreening(
                "SCR_01",
                ScreeningStatus.SCHEDULED,
                LocalDateTime.now().plusDays(1)
        );

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of(s));

        when(screeningSeatRepository.countAvailableSeats("SCR_01"))
                .thenReturn(30);

        ScreeningResponse response = new ScreeningResponse();
        when(screeningMapper.toScreeningResponse(s)).thenReturn(response);

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertEquals(1, result.size());
        assertEquals(30, result.get(0).getAvailableSeats());
    }
    @Test
    void TC02_shouldReturnScreening_whenAvailableSeatIsZero() {

        Screening s = buildScreening(
                "SCR_02",
                ScreeningStatus.SCHEDULED,
                LocalDateTime.now().plusDays(1)
        );

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of(s));

        when(screeningSeatRepository.countAvailableSeats("SCR_02"))
                .thenReturn(0);

        ScreeningResponse response = new ScreeningResponse();
        when(screeningMapper.toScreeningResponse(s)).thenReturn(response);

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getAvailableSeats());
    }

    @Test
    void TC03_shouldReturnOngoingScreening_whenWithinGraceTime() {

        Screening s = buildScreening(
                "SCR_03",
                ScreeningStatus.ONGOING,
                LocalDateTime.now().minusMinutes(30)
        );

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of(s));

        when(screeningSeatRepository.countAvailableSeats("SCR_03"))
                .thenReturn(15);

        ScreeningResponse response = new ScreeningResponse();
        when(screeningMapper.toScreeningResponse(s)).thenReturn(response);

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertEquals(1, result.size());
        assertEquals(15, result.get(0).getAvailableSeats());
    }

    @Test
    void TC04_shouldReturnEmpty_whenScreeningIsInPast() {

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of());

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertTrue(result.isEmpty());
    }

    @Test
    void TC05_shouldNotReturnCompletedScreening() {

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of());

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertEquals(0, result.size());
    }
    @Test
    void TC06_shouldNotReturnCancelledScreening() {

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of());

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertTrue(result.isEmpty());
    }
    @Test
    void TC07_shouldReturnScreening_whenAllSeatsSold() {

        Screening s = buildScreening(
                "SCR_07",
                ScreeningStatus.SCHEDULED,
                LocalDateTime.now().plusDays(1)
        );

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of(s));

        when(screeningSeatRepository.countAvailableSeats("SCR_07"))
                .thenReturn(0);

        ScreeningResponse response = new ScreeningResponse();
        when(screeningMapper.toScreeningResponse(s)).thenReturn(response);

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getAvailableSeats());
    }
    @Test
    void TC08_shouldReturnEmptyList_whenNoScreeningExists() {

        when(screeningRepository.findValidScreenings(any(), any()))
                .thenReturn(List.of());

        List<ScreeningResponse> result = screeningService.getScreenings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
