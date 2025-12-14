package com.theatermgnt.theatermgnt.screeningSeat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.screeningSeat.dto.response.ScreeningSeatResponse;
import com.theatermgnt.theatermgnt.screeningSeat.entity.ScreeningSeat;
import com.theatermgnt.theatermgnt.screeningSeat.mapper.ScreeningSeatMapper;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;

@ExtendWith(MockitoExtension.class)
class ScreeningSeatServiceImplTest {
    @InjectMocks
    private ScreeningSeatServiceImpl screeningSeatService;

    @Mock
    private ScreeningSeatRepository screeningSeatRepository;

    @Mock
    private ScreeningSeatMapper screeningSeatMapper;

    @Test
    void getScreeningSeatsByScreeningId_success() {
        String screeningId = "SCR-001";

        ScreeningSeat seat = new ScreeningSeat();
        ScreeningSeatResponse response = new ScreeningSeatResponse();

        when(screeningSeatRepository.findByScreeningId(screeningId)).thenReturn(List.of(seat));
        when(screeningSeatMapper.toScreeningSeatResponse(seat)).thenReturn(response);

        List<ScreeningSeatResponse> result = screeningSeatService.getScreeningSeatsByScreeningId(screeningId);

        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
    }

    @Test
    void getScreeningSeatsByScreeningId_emptyList() {
        String screeningId = "SCR-002";

        when(screeningSeatRepository.findByScreeningId(screeningId)).thenReturn(Collections.emptyList());

        List<ScreeningSeatResponse> result = screeningSeatService.getScreeningSeatsByScreeningId(screeningId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(screeningSeatMapper, never()).toScreeningSeatResponse(any());
    }
}
