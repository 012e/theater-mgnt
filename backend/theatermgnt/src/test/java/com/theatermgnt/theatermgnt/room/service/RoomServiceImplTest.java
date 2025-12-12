package com.theatermgnt.theatermgnt.room.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.cinema.entity.Cinema;
import com.theatermgnt.theatermgnt.cinema.repository.CinemaRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.room.dto.request.RoomCreationRequest;
import com.theatermgnt.theatermgnt.room.dto.request.RoomUpdateRequest;
import com.theatermgnt.theatermgnt.room.dto.response.RoomResponse;
import com.theatermgnt.theatermgnt.room.entity.Room;
import com.theatermgnt.theatermgnt.room.mapper.RoomMapper;
import com.theatermgnt.theatermgnt.room.repository.RoomRepository;
import com.theatermgnt.theatermgnt.seat.dto.request.SeatRequest;
import com.theatermgnt.theatermgnt.seat.service.SeatService;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    RoomRepository roomRepository;

    @Mock
    CinemaRepository cinemasRepository;

    @Mock
    RoomMapper roomMapper;

    @Mock
    SeatService seatService;

    @InjectMocks
    RoomServiceImpl roomService;

    @BeforeEach
    void setUp() {
        // nothing special for now
    }

    @Test
    void createRoom_whenCinemaNotFound_shouldThrow() {
        RoomCreationRequest request = RoomCreationRequest.builder()
                .cinemaId("cinema-1")
                .name("Room A")
                .roomType(null)
                .build();

        when(cinemasRepository.findById("cinema-1")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.createRoom(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CINEMA_NOT_EXISTED);

        verifyNoInteractions(roomRepository);
        verifyNoInteractions(roomMapper);
        verifyNoInteractions(seatService);
    }

    @Test
    void createRoom_whenRoomNameExists_shouldThrow() {
        RoomCreationRequest request = RoomCreationRequest.builder()
                .cinemaId("cinema-1")
                .name("Room A")
                .roomType(null)
                .build();

        Cinema cinema = Cinema.builder().id("cinema-1").name("C1").build();
        when(cinemasRepository.findById("cinema-1")).thenReturn(Optional.of(cinema));
        when(roomRepository.existsByNameAndCinemaId("Room A", "cinema-1")).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> roomService.createRoom(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROOM_EXISTED);

        verify(roomRepository).existsByNameAndCinemaId("Room A", "cinema-1");
        verify(roomRepository, never()).save(any());
        verifyNoInteractions(seatService);
    }

    @Test
    void createRoom_whenNoSeats_shouldSaveAndReturnResponse() {
        RoomCreationRequest request = RoomCreationRequest.builder()
                .cinemaId("cinema-1")
                .name("Room A")
                .roomType(null)
                .seats(null)
                .build();

        Cinema cinema = Cinema.builder().id("cinema-1").name("C1").build();
        Room room = Room.builder().name("Room A").cinema(cinema).build();
        Room savedRoom = Room.builder().id("r-1").name("Room A").cinema(cinema).build();
        RoomResponse expectedResponse = RoomResponse.builder()
                .id("r-1")
                .name("Room A")
                .cinemaId("cinema-1")
                .cinemaName("C1")
                .build();

        when(cinemasRepository.findById("cinema-1")).thenReturn(Optional.of(cinema));
        when(roomRepository.existsByNameAndCinemaId("Room A", "cinema-1")).thenReturn(false);
        when(roomMapper.toRoom(request)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(savedRoom);
        when(roomMapper.toRoomResponse(savedRoom)).thenReturn(expectedResponse);

        RoomResponse actual = roomService.createRoom(request);

        assertThat(actual).isSameAs(expectedResponse);
        verify(seatService, never()).syncSeats(any(), any());
        verify(roomRepository).save(room);
    }

    @Test
    void createRoom_whenSeatsProvided_shouldCallSyncSeats() {
        SeatRequest seat = SeatRequest.builder()
                .rowChair("A")
                .seatNumber(1)
                .seatTypeId("st-1")
                .build();
        RoomCreationRequest request = RoomCreationRequest.builder()
                .cinemaId("cinema-1")
                .name("Room A")
                .roomType(null)
                .seats(List.of(seat))
                .build();

        Cinema cinema = Cinema.builder().id("cinema-1").name("C1").build();
        Room room = Room.builder().name("Room A").cinema(cinema).build();
        // simulate repository returns same instance
        RoomResponse expectedResponse =
                RoomResponse.builder().id("r-1").name("Room A").build();

        when(cinemasRepository.findById("cinema-1")).thenReturn(Optional.of(cinema));
        when(roomRepository.existsByNameAndCinemaId("Room A", "cinema-1")).thenReturn(false);
        when(roomMapper.toRoom(request)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toRoomResponse(room)).thenReturn(expectedResponse);

        RoomResponse actual = roomService.createRoom(request);

        assertThat(actual).isSameAs(expectedResponse);
        verify(seatService).syncSeats(room, request.getSeats());
    }

    @Test
    void updateRoom_whenNotFound_shouldThrow() {
        RoomUpdateRequest request =
                RoomUpdateRequest.builder().name("New").roomType(null).build();
        when(roomRepository.findById("r-1")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.updateRoom("r-1", request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROOM_NOT_EXISTED);
        verify(roomMapper, never()).updateRoom(any(), any());
    }

    @Test
    void updateRoom_whenNameConflict_shouldThrow() {
        Cinema cinema = Cinema.builder().id("c-1").name("C").build();
        Room existingRoom = Room.builder().id("r-1").name("Old").cinema(cinema).build();
        RoomUpdateRequest request =
                RoomUpdateRequest.builder().name("NewName").roomType(null).build();

        when(roomRepository.findById("r-1")).thenReturn(Optional.of(existingRoom));
        // mapper will be called first but we don't change the cinema
        doNothing().when(roomMapper).updateRoom(eq(existingRoom), eq(request));
        when(roomRepository.existsByNameAndCinemaIdAndIdNot("NewName", "c-1", "r-1"))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> roomService.updateRoom("r-1", request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROOM_EXISTED);

        verify(roomMapper).updateRoom(existingRoom, request);
        verify(seatService, never()).syncSeats(any(), any());
    }

    @Test
    void updateRoom_whenSeatsProvided_shouldSyncAndReturn() {
        Cinema cinema = Cinema.builder().id("c-1").name("C").build();
        Room existingRoom = Room.builder().id("r-1").name("Old").cinema(cinema).build();
        RoomUpdateRequest request = RoomUpdateRequest.builder()
                .name("NewName")
                .roomType(null)
                .seats(List.of(SeatRequest.builder()
                        .rowChair("A")
                        .seatNumber(1)
                        .seatTypeId("st-1")
                        .build()))
                .build();

        Room savedRoom = Room.builder().id("r-1").name("NewName").cinema(cinema).build();
        RoomResponse expected = RoomResponse.builder().id("r-1").name("NewName").build();

        when(roomRepository.findById("r-1")).thenReturn(Optional.of(existingRoom));
        doNothing().when(roomMapper).updateRoom(existingRoom, request);
        when(roomRepository.existsByNameAndCinemaIdAndIdNot("NewName", "c-1", "r-1"))
                .thenReturn(false);
        when(roomRepository.save(existingRoom)).thenReturn(savedRoom);
        when(roomMapper.toRoomResponseWithSeats(savedRoom)).thenReturn(expected);

        RoomResponse actual = roomService.updateRoom("r-1", request);

        assertThat(actual).isSameAs(expected);
        verify(seatService).syncSeats(existingRoom, request.getSeats());
        verify(roomMapper).updateRoom(existingRoom, request);
    }

    @Test
    void getRoomsByCinema_shouldMapAndReturn() {
        Cinema cinema = Cinema.builder().id("c-1").name("C").build();
        Room room = Room.builder().id("r-1").name("Room").cinema(cinema).build();
        RoomResponse resp = RoomResponse.builder().id("r-1").name("Room").build();

        when(roomRepository.findByCinemaId("c-1")).thenReturn(List.of(room));
        when(roomMapper.toRoomResponse(room)).thenReturn(resp);

        List<RoomResponse> result = roomService.getRoomsByCinema("c-1");

        assertThat(result).containsExactly(resp);
    }

    @Test
    void getRooms_shouldMapAndReturnAll() {
        Cinema cinema = Cinema.builder().id("c-1").name("C").build();
        Room room = Room.builder().id("r-1").name("Room").cinema(cinema).build();
        RoomResponse resp = RoomResponse.builder().id("r-1").name("Room").build();

        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomMapper.toRoomResponse(room)).thenReturn(resp);

        List<RoomResponse> result = roomService.getRooms();

        assertThat(result).containsExactly(resp);
    }

    @Test
    void getRoom_whenNotFound_shouldThrow() {
        when(roomRepository.findById("r-1")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.getRoom("r-1"));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROOM_NOT_EXISTED);
    }

    @Test
    void getRoom_whenFound_shouldReturnWithSeats() {
        Cinema cinema = Cinema.builder().id("c-1").name("C").build();
        Room room = Room.builder().id("r-1").name("Room").cinema(cinema).build();
        RoomResponse resp = RoomResponse.builder().id("r-1").name("Room").build();

        when(roomRepository.findById("r-1")).thenReturn(Optional.of(room));
        when(roomMapper.toRoomResponseWithSeats(room)).thenReturn(resp);

        RoomResponse actual = roomService.getRoom("r-1");

        assertThat(actual).isSameAs(resp);
    }

    @Test
    void deleteRoom_whenNotFound_shouldThrow() {
        when(roomRepository.existsById("r-1")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> roomService.deleteRoom("r-1"));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ROOM_NOT_EXISTED);
    }

    @Test
    void deleteRoom_whenExists_shouldDelete() {
        when(roomRepository.existsById("r-1")).thenReturn(true);

        roomService.deleteRoom("r-1");

        verify(roomRepository).deleteById("r-1");
    }
}
