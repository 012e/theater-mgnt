package com.theatermgnt.theatermgnt.priceConfig.service;

import com.theatermgnt.theatermgnt.priceConfig.dto.request.PriceConfigCreationRequest;
import com.theatermgnt.theatermgnt.priceConfig.dto.request.PriceConfigUpdateRequest;
import com.theatermgnt.theatermgnt.priceConfig.dto.response.PriceConfigResponse;

import java.util.List;

public interface PriceConfigService {
    PriceConfigResponse createPriceConfig(PriceConfigCreationRequest request);

    List<PriceConfigResponse> getPriceConfigsBySeatType(String seatTypeId);

    List<PriceConfigResponse> getPriceConfigs();

    PriceConfigResponse getPriceConfig(String priceConfigId);

    PriceConfigResponse updatePriceConfig(String priceConfigId, PriceConfigUpdateRequest request);

    void deletePriceConfig(String priceConfigId);
}
