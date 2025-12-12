package com.theatermgnt.theatermgnt.seat.service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.room.entity.Room;
import com.theatermgnt.theatermgnt.seat.dto.request.SeatRequest;
import com.theatermgnt.theatermgnt.seat.entity.Seat;
import com.theatermgnt.theatermgnt.seatType.entity.SeatType;

import java.util.List;
import java.util.Map;

public interface SeatService {
    void syncSeats(Room room, List<SeatRequest> seatRequests);
    Seat mapRequestToSeat(SeatRequest seatRequest, Room room,
                          Map<String, SeatType> seatTypeMap,
                          Map<String, Seat> currentSeatMap);
}
