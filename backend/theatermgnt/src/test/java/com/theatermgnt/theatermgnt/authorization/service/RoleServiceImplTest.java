package com.theatermgnt.theatermgnt.authorization.service;

import com.theatermgnt.theatermgnt.authorization.dto.request.RoleRequest;
import com.theatermgnt.theatermgnt.authorization.dto.response.RoleResponse;
import com.theatermgnt.theatermgnt.authorization.entity.Permission;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.authorization.mapper.RoleMapper;
import com.theatermgnt.theatermgnt.authorization.repository.PermissionRepository;
import com.theatermgnt.theatermgnt.authorization.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
