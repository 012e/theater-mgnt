package com.theatermgnt.theatermgnt.screeningSeat.service;

import com.theatermgnt.theatermgnt.screeningSeat.dto.request.ScreeningSeatCreationRequest;
import com.theatermgnt.theatermgnt.screeningSeat.dto.request.ScreeningSeatUpdateRequest;
import com.theatermgnt.theatermgnt.screeningSeat.dto.response.ScreeningSeatResponse;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScreeningSeatService {
    @Transactional(propagation = Propagation.REQUIRED)
    ScreeningSeatResponse createScreeningSeat(ScreeningSeatCreationRequest request);

    List<ScreeningSeatResponse> getScreeningSeatsByScreeningId(String screeningId);

    List<ScreeningSeatResponse> getScreeningSeatsBySeatId(String seatId);

    List<ScreeningSeatResponse> getScreeningSeats();

    ScreeningSeatResponse getScreeningSeat(String screeningSeatId);

    ScreeningSeatResponse updateScreeningSeat(String screeningSeatId, ScreeningSeatUpdateRequest request);

    void deleteScreeningSeat(String screeningSeatId);
}
