package com.theatermgnt.theatermgnt.combo.service;

import java.util.List;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboItemUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboItemResponse;

public interface ComboItemService {
    ComboItemResponse createComboItem(ComboItemCreationRequest request);

    List<ComboItemResponse> getComboItemsByCombo(String comboId);

    List<ComboItemResponse> getComboItems();

    ComboItemResponse getComboItem(String comboItemId);

    ComboItemResponse updateComboItem(String comboItemId, ComboItemUpdateRequest request);

    void deleteComboItem(String comboItemId);
}
