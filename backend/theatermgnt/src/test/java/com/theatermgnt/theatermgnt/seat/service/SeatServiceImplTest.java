package com.theatermgnt.theatermgnt.seat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.mockito.stubbing.Answer;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.room.entity.Room;
import com.theatermgnt.theatermgnt.seat.dto.request.SeatRequest;
import com.theatermgnt.theatermgnt.seat.entity.Seat;
import com.theatermgnt.theatermgnt.seat.mapper.SeatMapper;
import com.theatermgnt.theatermgnt.seat.repository.SeatRepository;
import com.theatermgnt.theatermgnt.seatType.entity.SeatType;
import com.theatermgnt.theatermgnt.seatType.repository.SeatTypeRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeatServiceImplTest {

    @Mock
    SeatRepository seatRepository;

    @Mock
    SeatTypeRepository seatTypeRepository;

    @Mock
    SeatMapper seatMapper;

    SeatServiceImpl seatService;

    @Captor
    ArgumentCaptor<List> listCaptor;

    @BeforeEach
    void setUp() {
        seatService = new SeatServiceImpl(seatRepository, seatTypeRepository, seatMapper);

        // default mapper behavior: copy primitive fields from request to seat
        doAnswer((Answer<Void>) invocation -> {
            Seat seat = invocation.getArgument(0);
            SeatRequest req = invocation.getArgument(1);
            if (req != null) {
                seat.setRowChair(req.getRowChair());
                seat.setSeatNumber(req.getSeatNumber());
            }
            return null;
        }).when(seatMapper).updateSeat(any(Seat.class), any(SeatRequest.class));

        // default saveAll behavior: return the incoming list as result
        doAnswer(invocation -> {
            // return same list
            return invocation.getArgument(0);
        }).when(seatRepository).saveAll(anyList());
    }

    @Test
    void testSyncSeats_createsNewSeats_whenNoExistingSeats() {
        Room room = new Room();
        room.setId("room1");

        List<Seat> currentSeats = Collections.emptyList();
        when(seatRepository.findByRoomId("room1")).thenReturn(currentSeats);

        SeatRequest r1 = SeatRequest.builder().id(null).rowChair("A").seatNumber(1).seatTypeId("t1").build();
        SeatRequest r2 = SeatRequest.builder().id(null).rowChair("B").seatNumber(2).seatTypeId("t2").build();
        List<SeatRequest> requests = Arrays.asList(r1, r2);

        // prepare seat types
        SeatType t1 = SeatType.builder().id("t1").typeName("TYPE1").build();
        SeatType t2 = SeatType.builder().id("t2").typeName("TYPE2").build();
        when(seatTypeRepository.findAllById(any())).thenReturn(Arrays.asList(t1, t2));

        when(seatRepository.countByRoomId("room1")).thenReturn(2L);

        seatService.syncSeats(room, requests);

        // verify saveAll called with two seats and they have correct seatType and values
        verify(seatRepository, times(1)).saveAll(listCaptor.capture());
        List<Seat> saved = listCaptor.getValue();
        assertNotNull(saved);
        assertEquals(2, saved.size());

        Set<String> rowChairs = saved.stream().map(Seat::getRowChair).collect(Collectors.toSet());
        assertTrue(rowChairs.contains("A"));
        assertTrue(rowChairs.contains("B"));

        // seatType assignment
        Set<String> types = saved.stream().map(s -> s.getSeatType().getId()).collect(Collectors.toSet());
        assertTrue(types.contains("t1"));
        assertTrue(types.contains("t2"));

        // room updated
        assertEquals(saved, room.getSeats());
        assertEquals(2, room.getTotalSeats());
    }

    @Test
    void testSyncSeats_updatesExistingSeats() {
        Room room = new Room();
        room.setId("room1");

        Seat existing = new Seat();
        existing.setId("s1");
        existing.setRowChair("A");
        existing.setSeatNumber(1);
        existing.setRoom(room);
        existing.setSeatType(SeatType.builder().id("old").typeName("OLD").build());

        List<Seat> currentSeats = Arrays.asList(existing);
        when(seatRepository.findByRoomId("room1")).thenReturn(currentSeats);

        SeatRequest req = SeatRequest.builder().id("s1").rowChair("A-updated").seatNumber(10).seatTypeId("t1").build();

        SeatType t1 = SeatType.builder().id("t1").typeName("TYPE1").build();
        when(seatTypeRepository.findAllById(any())).thenReturn(Arrays.asList(t1));
        when(seatRepository.countByRoomId("room1")).thenReturn(1L);

        seatService.syncSeats(room, Arrays.asList(req));

        verify(seatRepository, times(1)).saveAll(listCaptor.capture());
        List<Seat> saved = listCaptor.getValue();
        assertEquals(1, saved.size());
        Seat updated = saved.get(0);
        // should keep id
        assertEquals("s1", updated.getId());
        assertEquals("A-updated", updated.getRowChair());
        assertEquals(10, updated.getSeatNumber());
        assertEquals("t1", updated.getSeatType().getId());
    }

    @Test
    void testSyncSeats_deletesMissingSeats() {
        Room room = new Room();
        room.setId("room1");

        Seat s1 = new Seat();
        s1.setId("s1");
        s1.setRowChair("A");
        s1.setSeatNumber(1);
        s1.setRoom(room);

        Seat s2 = new Seat();
        s2.setId("s2");
        s2.setRowChair("B");
        s2.setSeatNumber(2);
        s2.setRoom(room);

        List<Seat> currentSeats = Arrays.asList(s1, s2);
        when(seatRepository.findByRoomId("room1")).thenReturn(currentSeats);

        // only send request for s1, omit s2 -> s2 should be deleted
        SeatRequest req = SeatRequest.builder().id("s1").rowChair("A").seatNumber(1).seatTypeId("t1").build();
        SeatType t1 = SeatType.builder().id("t1").typeName("TYPE1").build();
        when(seatTypeRepository.findAllById(any())).thenReturn(Arrays.asList(t1));
        when(seatRepository.countByRoomId("room1")).thenReturn(1L);

        seatService.syncSeats(room, Arrays.asList(req));

        // verify deleteAll called with list containing s2
        ArgumentCaptor<List> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository, times(1)).deleteAll(deleteCaptor.capture());
        List deleted = deleteCaptor.getValue();
        assertEquals(1, deleted.size());
        Object deletedSeat = deleted.get(0);
        assertTrue(deletedSeat instanceof Seat);
        assertEquals("s2", ((Seat) deletedSeat).getId());
    }

    @Test
    void testSyncSeats_handlesNullRequests_deletesAllExisting() {
        Room room = new Room();
        room.setId("room1");

        Seat s1 = new Seat();
        s1.setId("s1");
        s1.setRowChair("A");
        s1.setSeatNumber(1);
        s1.setRoom(room);

        List<Seat> currentSeats = Arrays.asList(s1);
        when(seatRepository.findByRoomId("room1")).thenReturn(currentSeats);

        when(seatTypeRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(seatRepository.countByRoomId("room1")).thenReturn(0L);

        // pass null -> method treats as empty list and will delete all existing seats
        seatService.syncSeats(room, null);

        ArgumentCaptor<List> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(seatRepository, times(1)).deleteAll(deleteCaptor.capture());
        List deleted = deleteCaptor.getValue();
        assertEquals(1, deleted.size());
        assertEquals("s1", ((Seat) deleted.get(0)).getId());

        // saveAll should be called with empty list
        verify(seatRepository, times(1)).saveAll(listCaptor.capture());
        List saved = listCaptor.getValue();
        assertTrue(saved.isEmpty());
    }

    @Test
    void testMapRequestToSeat_throwsWhenSeatTypeMissing() {
        Room room = new Room();
        room.setId("r1");

        SeatRequest req = SeatRequest.builder().id(null).rowChair("A").seatNumber(1).seatTypeId("missing").build();

        Map<String, SeatType> emptyMap = new HashMap<>();
        Map<String, Seat> currentMap = new HashMap<>();

        AppException ex = assertThrows(AppException.class, () -> seatService.mapRequestToSeat(req, room, emptyMap, currentMap));
        assertEquals(ErrorCode.SEATTYPE_NOT_EXISTED, ex.getErrorCode());
    }

    @Test
    void testMapRequestToSeat_mapsExistingSeat_preserveRoom() {
        Room room = new Room();
        room.setId("r1");

        Seat existing = new Seat();
        existing.setId("s1");
        existing.setRowChair("A");
        existing.setSeatNumber(1);
        existing.setRoom(room);

        Map<String, Seat> currentMap = new HashMap<>();
        currentMap.put("s1", existing);

        SeatRequest req = SeatRequest.builder().id("s1").rowChair("X").seatNumber(99).seatTypeId("t1").build();

        SeatType t1 = SeatType.builder().id("t1").typeName("TYPE1").build();
        Map<String, SeatType> seatTypeMap = Collections.singletonMap("t1", t1);

        Seat result = seatService.mapRequestToSeat(req, room, seatTypeMap, currentMap);
        // should return same instance
        assertSame(existing, result);
        // room preserved
        assertSame(room, result.getRoom());
        // mapper should have updated fields
        assertEquals("X", result.getRowChair());
        assertEquals(99, result.getSeatNumber());
        // seatType set
        assertEquals("t1", result.getSeatType().getId());
    }

    @Test
    void testMapRequestToSeat_newSeat_setsRoom_and_callsMapper() {
        Room room = new Room();
        room.setId("r1");

        Map<String, Seat> currentMap = new HashMap<>();
        SeatRequest req = SeatRequest.builder().id(null).rowChair("Z").seatNumber(5).seatTypeId("t1").build();

        SeatType t1 = SeatType.builder().id("t1").typeName("TYPE1").build();
        Map<String, SeatType> seatTypeMap = Collections.singletonMap("t1", t1);

        Seat result = seatService.mapRequestToSeat(req, room, seatTypeMap, currentMap);
        assertNotNull(result);
        assertSame(room, result.getRoom());
        assertEquals("Z", result.getRowChair());
        assertEquals(5, result.getSeatNumber());
        assertEquals("t1", result.getSeatType().getId());
    }
}

