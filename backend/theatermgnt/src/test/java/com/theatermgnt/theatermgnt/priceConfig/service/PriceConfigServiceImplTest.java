package com.theatermgnt.theatermgnt.priceConfig.service;

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

import com.theatermgnt.theatermgnt.priceConfig.dto.response.PriceConfigResponse;
import com.theatermgnt.theatermgnt.priceConfig.entity.PriceConfig;
import com.theatermgnt.theatermgnt.priceConfig.mapper.PriceConfigMapper;
import com.theatermgnt.theatermgnt.priceConfig.repository.PriceConfigRepository;
import com.theatermgnt.theatermgnt.seatType.repository.SeatTypeRepository;

@ExtendWith(MockitoExtension.class)
public class PriceConfigServiceImplTest {
    @InjectMocks
    PriceConfigServiceImpl priceConfigService;

    @Mock
    PriceConfigMapper priceConfigMapper;

    @Mock
    PriceConfigRepository priceConfigRepository;

    @Mock
    SeatTypeRepository seatTypeRepository;

    @Test
    void getPriceConfigsBySeatType_success() {
        String seatTypeId = "SEAT-VIP";

        PriceConfig priceConfig = new PriceConfig();
        PriceConfigResponse response = new PriceConfigResponse();

        when(priceConfigRepository.findBySeatTypeId(seatTypeId)).thenReturn(List.of(priceConfig));
        when(priceConfigMapper.toPriceConfigResponse(priceConfig)).thenReturn(response);

        List<PriceConfigResponse> result = priceConfigService.getPriceConfigsBySeatType(seatTypeId);

        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
    }

    @Test
    void getPriceConfigsBySeatType_emptyList() {
        String seatTypeId = "SEAT-NOT-EXIST";

        when(priceConfigRepository.findBySeatTypeId(seatTypeId)).thenReturn(Collections.emptyList());

        List<PriceConfigResponse> result = priceConfigService.getPriceConfigsBySeatType(seatTypeId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(priceConfigMapper, never()).toPriceConfigResponse(any());
    }

    @Test
    void getPriceConfigsBySeatType_nullId_returnEmptyList() {
        // given
        when(priceConfigRepository.findBySeatTypeId(null)).thenReturn(Collections.emptyList());

        // when
        List<PriceConfigResponse> result = priceConfigService.getPriceConfigsBySeatType(null);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(priceConfigRepository).findBySeatTypeId(null);
        verifyNoInteractions(priceConfigMapper);
    }

    @Test
    void getPriceConfigsBySeatType_emptyId_returnEmptyList() {
        // given
        when(priceConfigRepository.findBySeatTypeId("")).thenReturn(Collections.emptyList());

        // when
        List<PriceConfigResponse> result = priceConfigService.getPriceConfigsBySeatType("");

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(priceConfigRepository).findBySeatTypeId("");
        verifyNoInteractions(priceConfigMapper);
    }
}
