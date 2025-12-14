package com.theatermgnt.theatermgnt.authorization.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.theatermgnt.theatermgnt.authorization.dto.request.PermissionRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.PermissionResponse;
import com.theatermgnt.theatermgnt.authorization.entity.Permission;
import com.theatermgnt.theatermgnt.authorization.mapper.PermissionMapper;
import com.theatermgnt.theatermgnt.authorization.repository.PermissionRepository;

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

        PermissionResponse result = permissionService.create(request);

        assertNotNull(result);
        assertEquals(response, result);

        verify(permissionMapper).toPermission(request);
        verify(permissionRepository).save(permission);
        verify(permissionMapper).toPermissionResponse(savedPermission);
    }

    @Test
    void getAll_success() {
        Permission p1 = new Permission();
        Permission p2 = new Permission();

        PermissionResponse r1 = new PermissionResponse();
        PermissionResponse r2 = new PermissionResponse();

        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
        when(permissionMapper.toPermissionResponse(p1)).thenReturn(r1);
        when(permissionMapper.toPermissionResponse(p2)).thenReturn(r2);

        List<PermissionResponse> result = permissionService.getAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(r1));
        assertTrue(result.contains(r2));

        verify(permissionRepository).findAll();
        verify(permissionMapper).toPermissionResponse(p1);
        verify(permissionMapper).toPermissionResponse(p2);
    }

    @Test
    void delete_success() {
        // Arrange
        String permissionId = "PERMISSION_READ";

        // Act
        permissionService.delete(permissionId);

        // Assert
        verify(permissionRepository).deleteById(permissionId);
    }
}
