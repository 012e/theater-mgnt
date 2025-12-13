package com.theatermgnt.theatermgnt.authentication.service;

import com.theatermgnt.theatermgnt.authorization.entity.Permission;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.staff.entity.Staff;
import com.theatermgnt.theatermgnt.staff.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {
    @InjectMocks
    TokenServiceImpl tokenService;

    @Mock
    StaffRepository staffRepository;

    @BeforeEach
    void setUp() {
        tokenService.VALID_DURATION = 3600;
        tokenService.SIGNER_KEY = "test-sign-key-test-sign-key-test-sign-key-test-sign-key";
    }

    @Test
    void buildScope_withRoleAndPermission_success() {
        Permission permission = new Permission();
        permission.setName("BOOKING_CREATE");

        Role role = new Role();
        role.setName("ADMIN");
        role.setPermissions(Set.of(permission));

        Staff staff = new Staff();
        staff.setRoles(Set.of(role));

        String scope = tokenService.buildScope(staff);

        assertEquals("ROLE_ADMIN BOOKING_CREATE", scope);
    }
}
