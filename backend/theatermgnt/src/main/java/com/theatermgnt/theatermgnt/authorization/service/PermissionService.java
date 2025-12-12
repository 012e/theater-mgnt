package com.theatermgnt.theatermgnt.authorization.service;

import java.util.List;

import com.theatermgnt.theatermgnt.authorization.dto.request.PermissionRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.PermissionResponse;

public interface PermissionService {
    PermissionResponse create(PermissionRequest request);

    List<PermissionResponse> getAll();

    void delete(String permission);
}
