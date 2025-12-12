package com.theatermgnt.theatermgnt.combo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboItemResponse;
import com.theatermgnt.theatermgnt.combo.entity.Combo;
import com.theatermgnt.theatermgnt.combo.entity.ComboItem;
import com.theatermgnt.theatermgnt.combo.mapper.ComboItemMapper;
import com.theatermgnt.theatermgnt.combo.repository.ComboItemRepository;
import com.theatermgnt.theatermgnt.combo.repository.ComboRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ComboItemServiceImplTest {

    @InjectMocks
    ComboItemServiceImpl comboItemService;

    @Mock
    ComboItemRepository comboItemRepository;

    @Mock
    ComboRepository comboRepository;

    @Mock
    ComboItemMapper comboItemMapper;

    // helper builders
    private Combo sampleCombo() {
        return Combo.builder()
                .name("Snack Combo")
                .description("Tasty")
                .price(BigDecimal.valueOf(10))
                .imageUrl("url")
                .build();
    }

    private ComboItem sampleComboItem(String name) {
        return ComboItem.builder().name(name).quantity(2).combo(sampleCombo()).build();
    }

    private ComboItemCreationRequest sampleCreationRequest() {
        return ComboItemCreationRequest.builder()
                .name("Popcorn")
                .comboId("combo-1")
                .quantity(2)
                .build();
    }

    private ComboItemUpdateRequest sampleUpdateRequest() {
        return ComboItemUpdateRequest.builder()
                .name("Large Popcorn")
                .quantity(3)
                .build();
    }

    @Test
    void createComboItem_whenComboNotFound_thenThrow() {
        ComboItemCreationRequest req = sampleCreationRequest();
        when(comboRepository.findById(req.getComboId())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> comboItemService.createComboItem(req));
        assertEquals(ErrorCode.COMBO_NOT_EXISTED, ex.getErrorCode());

        verify(comboRepository).findById(req.getComboId());
        verifyNoMoreInteractions(comboItemRepository, comboItemMapper);
    }

    @Test
    void createComboItem_whenNameExists_thenThrow() {
        ComboItemCreationRequest req = sampleCreationRequest();
        when(comboRepository.findById(req.getComboId())).thenReturn(Optional.of(sampleCombo()));
        when(comboItemRepository.existsByNameAndComboId(req.getName(), req.getComboId()))
                .thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> comboItemService.createComboItem(req));
        assertEquals(ErrorCode.COMBO_ITEM_EXISTED, ex.getErrorCode());

        verify(comboRepository).findById(req.getComboId());
        verify(comboItemRepository).existsByNameAndComboId(req.getName(), req.getComboId());
        verifyNoMoreInteractions(comboItemMapper);
    }

    @Test
    void createComboItem_success() {
        ComboItemCreationRequest req = sampleCreationRequest();
        Combo combo = sampleCombo();
        when(comboRepository.findById(req.getComboId())).thenReturn(Optional.of(combo));
        when(comboItemRepository.existsByNameAndComboId(req.getName(), req.getComboId()))
                .thenReturn(false);

        ComboItem mapped = ComboItem.builder()
                .name(req.getName())
                .quantity(req.getQuantity())
                .build();
        when(comboItemMapper.toComboItem(req)).thenReturn(mapped);

        ComboItem saved = ComboItem.builder()
                .name(req.getName())
                .quantity(req.getQuantity())
                .combo(combo)
                .build();

        // capture saved argument to assert combo was set
        ArgumentCaptor<ComboItem> captor = ArgumentCaptor.forClass(ComboItem.class);
        when(comboItemRepository.save(captor.capture())).thenReturn(saved);
        when(comboItemMapper.toComboItemResponse(saved))
                .thenReturn(ComboItemResponse.builder()
                        .id(saved.getId())
                        .comboName(combo.getName())
                        .name(saved.getName())
                        .quantity(saved.getQuantity())
                        .build());

        ComboItemResponse resp = comboItemService.createComboItem(req);

        assertNotNull(resp);
        assertEquals(saved.getId(), resp.getId());
        assertEquals(combo.getName(), resp.getComboName());
        assertEquals(saved.getName(), resp.getName());

        ComboItem savedArg = captor.getValue();
        assertNotNull(savedArg.getCombo(), "combo should be set on saved entity");
        assertEquals(combo.getId(), savedArg.getCombo().getId());

        verify(comboRepository).findById(req.getComboId());
        verify(comboItemRepository).existsByNameAndComboId(req.getName(), req.getComboId());
        verify(comboItemMapper).toComboItem(req);
        verify(comboItemRepository).save(any(ComboItem.class));
        verify(comboItemMapper).toComboItemResponse(saved);
    }

    @Test
    void getComboItemsByCombo_returnsMappedList() {
        ComboItem it1 = sampleComboItem("A");
        ComboItem it2 = sampleComboItem("B");
        when(comboItemRepository.findByComboId("combo-1")).thenReturn(List.of(it1, it2));

        when(comboItemMapper.toComboItemResponse(it1))
                .thenReturn(ComboItemResponse.builder()
                        .id(it1.getId())
                        .comboName(it1.getCombo().getName())
                        .name(it1.getName())
                        .quantity(it1.getQuantity())
                        .build());
        when(comboItemMapper.toComboItemResponse(it2))
                .thenReturn(ComboItemResponse.builder()
                        .id(it2.getId())
                        .comboName(it2.getCombo().getName())
                        .name(it2.getName())
                        .quantity(it2.getQuantity())
                        .build());

        List<ComboItemResponse> responses = comboItemService.getComboItemsByCombo("combo-1");
        assertEquals(2, responses.size());

        verify(comboItemRepository).findByComboId("combo-1");
        verify(comboItemMapper, times(2)).toComboItemResponse(any(ComboItem.class));
    }

    @Test
    void getComboItemsByCombo_empty() {
        when(comboItemRepository.findByComboId("combo-empty")).thenReturn(List.of());
        List<ComboItemResponse> responses = comboItemService.getComboItemsByCombo("combo-empty");
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(comboItemRepository).findByComboId("combo-empty");
        verifyNoInteractions(comboItemMapper);
    }

    @Test
    void getComboItems_returnsAllMapped() {
        ComboItem it1 = sampleComboItem("A");
        when(comboItemRepository.findAll()).thenReturn(List.of(it1));
        when(comboItemMapper.toComboItemResponse(it1))
                .thenReturn(ComboItemResponse.builder()
                        .id(it1.getId())
                        .comboName(it1.getCombo().getName())
                        .name(it1.getName())
                        .quantity(it1.getQuantity())
                        .build());

        List<ComboItemResponse> responses = comboItemService.getComboItems();
        assertEquals(1, responses.size());

        verify(comboItemRepository).findAll();
        verify(comboItemMapper).toComboItemResponse(it1);
    }

    @Test
    void getComboItem_notFound_thenThrow() {
        when(comboItemRepository.findById("missing")).thenReturn(Optional.empty());
        AppException ex = assertThrows(AppException.class, () -> comboItemService.getComboItem("missing"));
        assertEquals(ErrorCode.COMBO_ITEM_NOT_EXISTED, ex.getErrorCode());
        verify(comboItemRepository).findById("missing");
    }

    @Test
    void getComboItem_success() {
        ComboItem it = sampleComboItem("A");
        when(comboItemRepository.findById("i1")).thenReturn(Optional.of(it));
        when(comboItemMapper.toComboItemResponse(it))
                .thenReturn(ComboItemResponse.builder()
                        .id(it.getId())
                        .comboName(it.getCombo().getName())
                        .name(it.getName())
                        .quantity(it.getQuantity())
                        .build());

        ComboItemResponse resp = comboItemService.getComboItem("i1");
        assertNotNull(resp);
        assertEquals(it.getId(), resp.getId());
        verify(comboItemRepository).findById("i1");
        verify(comboItemMapper).toComboItemResponse(it);
    }

    @Test
    void updateComboItem_notFound_thenThrow() {
        when(comboItemRepository.findById("i-missing")).thenReturn(Optional.empty());
        AppException ex = assertThrows(
                AppException.class, () -> comboItemService.updateComboItem("i-missing", sampleUpdateRequest()));
        assertEquals(ErrorCode.COMBO_ITEM_NOT_EXISTED, ex.getErrorCode());
        verify(comboItemRepository).findById("i-missing");
    }

    @Test
    void updateComboItem_success() {
        ComboItem existing = sampleComboItem("Old");
        when(comboItemRepository.findById("i1")).thenReturn(Optional.of(existing));

        // mapper.updateComboItem should be invoked; we simulate it by letting it set fields via doAnswer
        doAnswer(invocation -> {
                    ComboItem target = invocation.getArgument(0);
                    ComboItemUpdateRequest req = invocation.getArgument(1);
                    target.setName(req.getName());
                    target.setQuantity(req.getQuantity());
                    return null;
                })
                .when(comboItemMapper)
                .updateComboItem(any(ComboItem.class), any(ComboItemUpdateRequest.class));

        when(comboItemRepository.save(existing)).thenReturn(existing);
        when(comboItemMapper.toComboItemResponse(existing))
                .thenReturn(ComboItemResponse.builder()
                        .id(existing.getId())
                        .comboName(existing.getCombo().getName())
                        .name("Large Popcorn")
                        .quantity(3)
                        .build());

        ComboItemResponse resp = comboItemService.updateComboItem("i1", sampleUpdateRequest());
        assertNotNull(resp);
        assertEquals("Large Popcorn", resp.getName());
        assertEquals(3, resp.getQuantity());

        verify(comboItemRepository).findById("i1");
        verify(comboItemMapper).updateComboItem(existing, sampleUpdateRequest());
        verify(comboItemRepository).save(existing);
        verify(comboItemMapper).toComboItemResponse(existing);
    }

    @Test
    void deleteComboItem_notFound_thenThrow() {
        when(comboItemRepository.existsById("x")).thenReturn(false);
        AppException ex = assertThrows(AppException.class, () -> comboItemService.deleteComboItem("x"));
        assertEquals(ErrorCode.COMBO_ITEM_NOT_EXISTED, ex.getErrorCode());
        verify(comboItemRepository).existsById("x");
    }

    @Test
    void deleteComboItem_success() {
        when(comboItemRepository.existsById("i1")).thenReturn(true);
        doNothing().when(comboItemRepository).deleteById("i1");

        comboItemService.deleteComboItem("i1");

        verify(comboItemRepository).existsById("i1");
        verify(comboItemRepository).deleteById("i1");
    }
}
