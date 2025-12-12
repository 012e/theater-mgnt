package com.theatermgnt.theatermgnt.seatType.service;

import java.util.List;

import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeCreationRequest;
import com.theatermgnt.theatermgnt.seatType.dto.request.SeatTypeUpdateRequest;
import com.theatermgnt.theatermgnt.seatType.dto.response.SeatTypeResponse;

public interface SeatTypeService {
    SeatTypeResponse createSeatType(SeatTypeCreationRequest request);

    List<SeatTypeResponse> getSeatTypes();

    SeatTypeResponse getSeatType(String seatTypeId);

    void deleteSeatType(String seatTypeId);

    SeatTypeResponse updateSeatType(String seatTypeId, SeatTypeUpdateRequest request);
}
