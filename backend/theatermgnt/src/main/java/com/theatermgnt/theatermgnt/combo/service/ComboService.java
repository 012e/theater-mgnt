package com.theatermgnt.theatermgnt.combo.service;

import com.theatermgnt.theatermgnt.combo.dto.request.ComboCreationRequest;
import com.theatermgnt.theatermgnt.combo.dto.request.ComboUpdateRequest;
import com.theatermgnt.theatermgnt.combo.dto.response.ComboResponse;

import java.util.List;

public interface ComboService {
    ComboResponse createCombo(ComboCreationRequest request);

    List<ComboResponse> getCombos();

    ComboResponse getCombo(String comboId);

    void deleteCombo(String comboId);

    ComboResponse updateCombo(String comboId, ComboUpdateRequest request);
}
