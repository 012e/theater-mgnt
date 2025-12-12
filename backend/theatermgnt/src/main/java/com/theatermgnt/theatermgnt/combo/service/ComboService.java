package com.theatermgnt.theatermgnt.combo.service;

import java.util.List;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboResponse;

public interface ComboService {
    ComboResponse createCombo(ComboCreationRequest request);

    List<ComboResponse> getCombos();

    ComboResponse getCombo(String comboId);

    void deleteCombo(String comboId);

    ComboResponse updateCombo(String comboId, ComboUpdateRequest request);
}
