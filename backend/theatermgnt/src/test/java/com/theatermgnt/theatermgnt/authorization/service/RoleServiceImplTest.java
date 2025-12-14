package com.theatermgnt.theatermgnt.authorization.service;

import com.theatermgnt.theatermgnt.authorization.dto.request.RoleRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.RoleResponse;
import com.theatermgnt.theatermgnt.authorization.entity.Permission;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.authorization.mapper.RoleMapper;
import com.theatermgnt.theatermgnt.authorization.repository.PermissionRepository;
import com.theatermgnt.theatermgnt.authorization.repository.RoleRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.management.relation.RoleResult;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {
    @InjectMocks
    RoleServiceImpl roleService;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PermissionRepository permissionRepository;

    @Mock
    RoleMapper roleMapper;

    @Test
    void createRole_success()
    {
        //arrange
        RoleRequest request = new RoleRequest();
        request.setPermissions(Set.of("PERM1", "PERM2"));

        Role role = new Role();
        Role savedRole = new Role();
        RoleResponse response = new RoleResponse();

        when(roleMapper.toRole(request)).thenReturn(role);
        when(permissionRepository.findAllById(request.getPermissions()))
                .thenReturn(List.of(new Permission(), new Permission()));

        when(roleRepository.save(role)).thenReturn(savedRole);
        when(roleMapper.toRoleResponse(savedRole)).thenReturn(response);

        //act
        RoleResponse result = roleService.create(request);

        //assert
        assertNotNull(result);

        verify(roleMapper).toRole(request);
        verify(permissionRepository).findAllById(request.getPermissions());
        verify(roleRepository).save(role);
        verify(roleMapper).toRoleResponse(savedRole);
    }

    @Test
    void getAllRoles_success()
    {
        //arrange
        Role role1 = new Role();
        Role role2 = new Role();

        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));
        when(roleMapper.toRoleResponse(any(Role.class)))
                .thenReturn(new RoleResponse());

        //act
        List<RoleResponse> result = roleService.getAll();

        //assert
        assertEquals(2, result.size());

        verify(roleRepository).findAll();
        verify(roleMapper, times(2)).toRoleResponse(any(Role.class));
    }

    @Test
    void updateRole_success()
    {
        //arrange
        String roleId = "ROLE_1";

        RoleRequest request = new RoleRequest();
        request.setPermissions(Set.of("ROLE_1"));

        Role existingRole = new Role();
        Role savedRole = new Role();
        RoleResponse response = new RoleResponse();

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(existingRole));

        when(permissionRepository.findAllById(request.getPermissions()))
                .thenReturn(List.of(new Permission()));

        when(roleRepository.save(existingRole)).thenReturn(savedRole);
        when(roleMapper.toRoleResponse(savedRole)).thenReturn(response);

        //act
        RoleResponse result = roleService.update(roleId, request);

        //assert
        assertNotNull(result);

        verify(roleRepository).findById(roleId);
        verify(permissionRepository).findAllById(request.getPermissions());
        verify(roleMapper).toRoleResponse(savedRole);
        verify(roleRepository).save(existingRole);
        verify(roleMapper).updateRole(request, existingRole);
    }

    @Test
    void updateRole_roleNotFound(){
        //arrange
        String roleId = "NOT_EXIST";

        RoleRequest request = new RoleRequest();

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        //act + assert
        AppException exception
                = assertThrows(AppException.class, ()->roleService.update(roleId, request));

        assertEquals(ErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
        verify(roleRepository).findById(roleId);
        verifyNoMoreInteractions(roleRepository, permissionRepository, roleMapper);
    }


}

