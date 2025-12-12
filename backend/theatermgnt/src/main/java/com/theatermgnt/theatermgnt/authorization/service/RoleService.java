package com.theatermgnt.theatermgnt.authorization.service;

import java.util.List;

import com.theatermgnt.theatermgnt.authorization.dto.request.RoleRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.RoleResponse;

public interface RoleService {
    RoleResponse create(RoleRequest request);

    List<RoleResponse> getAll();

    RoleResponse update(String roleId, RoleRequest request);

    void delete(String roleId);
}
