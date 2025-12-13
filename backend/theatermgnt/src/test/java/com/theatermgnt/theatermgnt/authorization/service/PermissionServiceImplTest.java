package com.theatermgnt.theatermgnt.authorization.service;

import com.theatermgnt.theatermgnt.authorization.dto.request.PermissionRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.PermissionResponse;
import com.theatermgnt.theatermgnt.authorization.entity.Permission;
import com.theatermgnt.theatermgnt.authorization.mapper.PermissionMapper;
import com.theatermgnt.theatermgnt.authorization.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceImplTest {
    @InjectMocks
    PermissionServiceImpl permissionService;

    @Mock
    PermissionRepository permissionRepository;

    @Mock
    PermissionMapper permissionMapper;

    @Test
    void create_success() {
        PermissionRequest request = new PermissionRequest();
        Permission permission = new Permission();
        Permission savedPermission = new Permission();
        PermissionResponse response = new PermissionResponse();

        when(permissionMapper.toPermission(request)).thenReturn(permission);
        when(permissionRepository.save(permission)).thenReturn(savedPermission);
        when(permissionMapper.toPermissionResponse(savedPermission)).thenReturn(response);

        PermissionResponse result =  permissionService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        verify(permissionMapper).toPermission(request);
        verify(permissionRepository).save(permission);
        verify(permissionMapper).toPermissionResponse(savedPermission);
    }
}
