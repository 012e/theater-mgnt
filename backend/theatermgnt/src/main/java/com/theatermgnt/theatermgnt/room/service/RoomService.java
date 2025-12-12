package com.theatermgnt.theatermgnt.room.service;

import com.theatermgnt.theatermgnt.room.dto.request.RoomCreationRequest;
import com.theatermgnt.theatermgnt.room.dto.request.RoomUpdateRequest;
import com.theatermgnt.theatermgnt.room.dto.response.RoomResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoomService {
    @Transactional
    RoomResponse createRoom(RoomCreationRequest request);

    @Transactional
    RoomResponse updateRoom(String roomId, RoomUpdateRequest request);

    List<RoomResponse> getRoomsByCinema(String cinemaId);

    List<RoomResponse> getRooms();

    RoomResponse getRoom(String roomId);

    void deleteRoom(String roomId);
}
