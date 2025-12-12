package com.theatermgnt.theatermgnt.movie.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateAgeRatingRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.AgeRatingResponse;
import com.theatermgnt.theatermgnt.movie.entity.AgeRating;
import com.theatermgnt.theatermgnt.movie.mapper.AgeRatingMapper;
import com.theatermgnt.theatermgnt.movie.repository.AgeRatingRepository;

@ExtendWith(MockitoExtension.class)
class AgeRatingServiceImplTest {

    @Mock
    AgeRatingRepository ageRatingRepository;

    @Mock
    AgeRatingMapper ageRatingMapper;

    @InjectMocks
    AgeRatingServiceImpl ageRatingService;

    CreateAgeRatingRequest request;

    @BeforeEach
    void setUp() {
        request = CreateAgeRatingRequest.builder()
                .id("ar-1")
                .code("PG-13")
                .description("Parental Guidance")
                .build();
    }

    @Test
    void createAgeRating_success() {
        // Arrange
        when(ageRatingRepository.existsById(request.getId())).thenReturn(false);
        when(ageRatingRepository.findByCode(request.getCode())).thenReturn(Optional.empty());

        AgeRating mapped = AgeRating.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .build();

        AgeRating saved = AgeRating.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .build();
        // simulate id assigned by DB
        saved.setId(request.getId());

        AgeRatingResponse response = AgeRatingResponse.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .description(saved.getDescription())
                .build();

        when(ageRatingMapper.toAgeRating(request)).thenReturn(mapped);
        when(ageRatingRepository.save(mapped)).thenReturn(saved);
        when(ageRatingMapper.toAgeRatingResponse(saved)).thenReturn(response);

        // Act
        AgeRatingResponse result = ageRatingService.createAgeRating(request);

        // Assert
        assertNotNull(result);
        assertEquals(response.getId(), result.getId());
        assertEquals(response.getCode(), result.getCode());
        assertEquals(response.getDescription(), result.getDescription());

        verify(ageRatingRepository).existsById(request.getId());
        verify(ageRatingRepository).findByCode(request.getCode());
        verify(ageRatingMapper).toAgeRating(request);
        verify(ageRatingRepository).save(mapped);
        verify(ageRatingMapper).toAgeRatingResponse(saved);
    }

    @Test
    void createAgeRating_idAlreadyExists_throwsAppException() {
        when(ageRatingRepository.existsById(request.getId())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> ageRatingService.createAgeRating(request));
        assertEquals(ErrorCode.AGERATING_EXISTED, ex.getErrorCode());

        verify(ageRatingRepository).existsById(request.getId());
        verify(ageRatingRepository, never()).findByCode(any());
        verify(ageRatingRepository, never()).save(any());
        verify(ageRatingMapper, never()).toAgeRating(any());
    }

    @Test
    void createAgeRating_codeAlreadyExists_throwsAppException() {
        when(ageRatingRepository.existsById(request.getId())).thenReturn(false);
        when(ageRatingRepository.findByCode(request.getCode()))
                .thenReturn(Optional.of(AgeRating.builder().build()));

        AppException ex = assertThrows(AppException.class, () -> ageRatingService.createAgeRating(request));
        assertEquals(ErrorCode.AGERATING_CODE_EXISTED, ex.getErrorCode());

        verify(ageRatingRepository).existsById(request.getId());
        verify(ageRatingRepository).findByCode(request.getCode());
        verify(ageRatingRepository, never()).save(any());
        verify(ageRatingMapper, never()).toAgeRating(any());
    }

    @Test
    void getAllAgeRatings_returnsMappedResponses() {
        // Arrange
        AgeRating a1 = AgeRating.builder().code("G").description("General").build();
        a1.setId("id-1");
        AgeRating a2 =
                AgeRating.builder().code("PG").description("Parental Guidance").build();
        a2.setId("id-2");

        List<AgeRating> list = List.of(a1, a2);
        AgeRatingResponse r1 = AgeRatingResponse.builder()
                .id(a1.getId())
                .code(a1.getCode())
                .description(a1.getDescription())
                .build();
        AgeRatingResponse r2 = AgeRatingResponse.builder()
                .id(a2.getId())
                .code(a2.getCode())
                .description(a2.getDescription())
                .build();
        List<AgeRatingResponse> respList = List.of(r1, r2);

        when(ageRatingRepository.findAll()).thenReturn(list);
        when(ageRatingMapper.toAgeRatingResponseList(list)).thenReturn(respList);

        // Act
        List<AgeRatingResponse> result = ageRatingService.getAllAgeRatings();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(respList, result);

        verify(ageRatingRepository).findAll();
        verify(ageRatingMapper).toAgeRatingResponseList(list);
    }

    @Test
    void getAgeRatingById_found_returnsMappedResponse() {
        AgeRating a = AgeRating.builder().code("G").description("General").build();
        a.setId("id-10");
        AgeRatingResponse resp = AgeRatingResponse.builder()
                .id(a.getId())
                .code(a.getCode())
                .description(a.getDescription())
                .build();

        when(ageRatingRepository.findById(a.getId())).thenReturn(Optional.of(a));
        when(ageRatingMapper.toAgeRatingResponse(a)).thenReturn(resp);

        AgeRatingResponse result = ageRatingService.getAgeRatingById(a.getId());

        assertNotNull(result);
        assertEquals(resp, result);

        verify(ageRatingRepository).findById(a.getId());
        verify(ageRatingMapper).toAgeRatingResponse(a);
    }

    @Test
    void getAgeRatingById_notFound_throwsAppException() {
        when(ageRatingRepository.findById("missing")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> ageRatingService.getAgeRatingById("missing"));
        assertEquals(ErrorCode.AGERATING_NOT_EXISTED, ex.getErrorCode());

        verify(ageRatingRepository).findById("missing");
        verify(ageRatingMapper, never()).toAgeRatingResponse(any());
    }

    @Test
    void getAgeRatingByCode_found_returnsMappedResponse() {
        AgeRating a = AgeRating.builder().code("R").description("Restricted").build();
        a.setId("id-20");
        AgeRatingResponse resp = AgeRatingResponse.builder()
                .id(a.getId())
                .code(a.getCode())
                .description(a.getDescription())
                .build();

        when(ageRatingRepository.findByCode(a.getCode())).thenReturn(Optional.of(a));
        when(ageRatingMapper.toAgeRatingResponse(a)).thenReturn(resp);

        AgeRatingResponse result = ageRatingService.getAgeRatingByCode(a.getCode());

        assertNotNull(result);
        assertEquals(resp, result);

        verify(ageRatingRepository).findByCode(a.getCode());
        verify(ageRatingMapper).toAgeRatingResponse(a);
    }

    @Test
    void getAgeRatingByCode_notFound_throwsAppException() {
        when(ageRatingRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> ageRatingService.getAgeRatingByCode("UNKNOWN"));
        assertEquals(ErrorCode.AGERATING_NOT_EXISTED, ex.getErrorCode());

        verify(ageRatingRepository).findByCode("UNKNOWN");
        verify(ageRatingMapper, never()).toAgeRatingResponse(any());
    }
}
