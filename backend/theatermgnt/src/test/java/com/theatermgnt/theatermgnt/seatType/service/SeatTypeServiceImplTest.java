package com.theatermgnt.theatermgnt.seatType.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeCreationRequest;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeUpdateRequest;
import com.theatermgnt.theatermgnt.seatType.dto.response.SeatTypeResponse;
import com.theatermgnt.theatermgnt.seatType.entity.SeatType;
import com.theatermgnt.theatermgnt.seatType.mapper.SeatTypeMapper;
import com.theatermgnt.theatermgnt.seatType.repository.SeatTypeRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeatTypeServiceImplTest {

    @Mock
    SeatTypeRepository seatTypeRepository;

    @Mock
    SeatTypeMapper seatTypeMapper;

    @InjectMocks
    SeatTypeServiceImpl seatTypeService;

    @Captor
    ArgumentCaptor<SeatType> seatTypeCaptor;

    @BeforeEach
    void setUp() {
        // default mapper update behavior: copy fields
        doAnswer(invocation -> {
                    SeatType target = invocation.getArgument(0);
                    SeatTypeUpdateRequest req = invocation.getArgument(1);
                    if (req != null) {
                        target.setTypeName(req.getTypeName());
                        target.setBasePriceModifier(BigDecimal.valueOf(req.getBasePriceModifier()));
                    }
                    return null;
                })
                .when(seatTypeMapper)
                .updateSeatType(any(SeatType.class), any(SeatTypeUpdateRequest.class));
    }

    @Test
    void createSeatType_success() {
        SeatTypeCreationRequest req = SeatTypeCreationRequest.builder()
                .typeName("VIP")
                .basePriceModifier(2.5)
                .build();

        SeatType mapped = SeatType.builder()
                .typeName("VIP")
                .basePriceModifier(BigDecimal.valueOf(2.5))
                .build();
        SeatType saved = SeatType.builder()
                .id("st1")
                .typeName("VIP")
                .basePriceModifier(BigDecimal.valueOf(2.5))
                .build();
        SeatTypeResponse resp = SeatTypeResponse.builder()
                .id("st1")
                .typeName("VIP")
                .basePriceModifier(BigDecimal.valueOf(2.5))
                .build();

        when(seatTypeRepository.existsByTypeName("VIP")).thenReturn(false);
        when(seatTypeMapper.toSeatType(req)).thenReturn(mapped);
        when(seatTypeRepository.save(mapped)).thenReturn(saved);
        when(seatTypeMapper.toSeatTypeResponse(saved)).thenReturn(resp);

        SeatTypeResponse result = seatTypeService.createSeatType(req);

        assertNotNull(result);
        assertEquals("st1", result.getId());
        assertEquals("VIP", result.getTypeName());
        assertEquals(0, result.getBasePriceModifier().compareTo(BigDecimal.valueOf(2.5)));

        verify(seatTypeRepository, times(1)).existsByTypeName("VIP");
        verify(seatTypeRepository, times(1)).save(mapped);
    }

    @Test
    void createSeatType_whenExist_thenThrow() {
        SeatTypeCreationRequest req = SeatTypeCreationRequest.builder()
                .typeName("VIP")
                .basePriceModifier(1.0)
                .build();
        when(seatTypeRepository.existsByTypeName(anyString())).thenReturn(true);
        AppException ex = assertThrows(AppException.class, () -> seatTypeService.createSeatType(req));
        assertEquals(ErrorCode.SEATTYPE_EXISTED, ex.getErrorCode());
    }

    @Test
    void getSeatTypes_returnsMappedList() {
        SeatType s1 = SeatType.builder()
                .id("a")
                .typeName("A")
                .basePriceModifier(BigDecimal.ONE)
                .build();
        SeatType s2 = SeatType.builder()
                .id("b")
                .typeName("B")
                .basePriceModifier(BigDecimal.valueOf(1.5))
                .build();

        SeatTypeResponse r1 = SeatTypeResponse.builder()
                .id("a")
                .typeName("A")
                .basePriceModifier(BigDecimal.ONE)
                .build();
        SeatTypeResponse r2 = SeatTypeResponse.builder()
                .id("b")
                .typeName("B")
                .basePriceModifier(BigDecimal.valueOf(1.5))
                .build();

        when(seatTypeRepository.findAll()).thenReturn(Arrays.asList(s1, s2));
        when(seatTypeMapper.toSeatTypeResponse(s1)).thenReturn(r1);
        when(seatTypeMapper.toSeatTypeResponse(s2)).thenReturn(r2);

        List<SeatTypeResponse> list = seatTypeService.getSeatTypes();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue(list.contains(r1));
        assertTrue(list.contains(r2));
    }

    @Test
    void getSeatType_success() {
        String seatTypeId = "SEAT-VIP";

        SeatType seatType = new SeatType();
        SeatTypeResponse response = new SeatTypeResponse();

        when(seatTypeRepository.findById(seatTypeId)).thenReturn(Optional.of(seatType));
        when(seatTypeMapper.toSeatTypeResponse(seatType)).thenReturn(response);

        SeatTypeResponse result = seatTypeService.getSeatType(seatTypeId);

        assertEquals(response, result);

        verify(seatTypeMapper).toSeatTypeResponse(seatType);
    }

    @Test
    void getSeatType_notFound_throwException() {
        String seatTypeId = "SEAT-NOT-EXIST";

        when(seatTypeRepository.findById(seatTypeId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> seatTypeService.getSeatType(seatTypeId));

        assertEquals(ErrorCode.SEATTYPE_NOT_EXISTED, exception.getErrorCode());

        verify(seatTypeMapper, never()).toSeatTypeResponse(any());
    }

    @Test
    void deleteSeatType_delegatesToRepository() {
        seatTypeService.deleteSeatType("to-delete");
        verify(seatTypeRepository, times(1)).deleteById("to-delete");
    }

    @Test
    void updateSeatType_success() {
        SeatType existing = SeatType.builder()
                .id("u1")
                .typeName("Old")
                .basePriceModifier(BigDecimal.ONE)
                .build();
        SeatType saved = SeatType.builder()
                .id("u1")
                .typeName("New")
                .basePriceModifier(BigDecimal.valueOf(3.0))
                .build();
        SeatTypeResponse resp = SeatTypeResponse.builder()
                .id("u1")
                .typeName("New")
                .basePriceModifier(BigDecimal.valueOf(3.0))
                .build();

        SeatTypeUpdateRequest req = SeatTypeUpdateRequest.builder()
                .typeName("New")
                .basePriceModifier(3.0)
                .build();

        when(seatTypeRepository.findById("u1")).thenReturn(Optional.of(existing));
        // mapper.updateSeatType will be executed via doAnswer in setUp
        when(seatTypeRepository.save(existing)).thenReturn(saved);
        when(seatTypeMapper.toSeatTypeResponse(saved)).thenReturn(resp);

        SeatTypeResponse out = seatTypeService.updateSeatType("u1", req);

        assertNotNull(out);
        assertEquals("u1", out.getId());
        assertEquals("New", out.getTypeName());

        verify(seatTypeMapper, times(1)).updateSeatType(existing, req);
        verify(seatTypeRepository, times(1)).save(existing);
    }

    @Test
    void updateSeatType_notFound_throws() {
        when(seatTypeRepository.findById("nope")).thenReturn(Optional.empty());
        SeatTypeUpdateRequest req = SeatTypeUpdateRequest.builder()
                .typeName("Any")
                .basePriceModifier(1.0)
                .build();
        AppException ex = assertThrows(AppException.class, () -> seatTypeService.updateSeatType("nope", req));
        assertEquals(ErrorCode.SEATTYPE_NOT_EXISTED, ex.getErrorCode());
    }
}
