package com.theatermgnt.theatermgnt.authentication.service;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.authentication.enums.AccountType;
import com.theatermgnt.theatermgnt.authorization.entity.Permission;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.staff.entity.Staff;
import com.theatermgnt.theatermgnt.staff.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {
    @InjectMocks
    TokenServiceImpl tokenService;

    @Mock
    StaffRepository staffRepository;

    @BeforeEach
    void setUp() {
        tokenService.VALID_DURATION = 3600;
        tokenService.SIGNER_KEY = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
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

    @Test
    void buildScope_noRoles_returnEmptyString() {
        Staff staff = new Staff();
        staff.setRoles(Set.of());

        String scope =  tokenService.buildScope(staff);
        assertEquals("", scope);
    }

    @Test
    void generateToken_internalAccount_success() {
        // given
        Account account = new Account();
        account.setId("acc-1");
        account.setAccountType(AccountType.INTERNAL);

        Permission p1 = new Permission();
        p1.setName("BOOKING_READ");

        Role role = new Role();
        role.setName("ADMIN");
        role.setPermissions(Set.of(p1));

        Staff staff = new Staff();
        staff.setRoles(Set.of(role));

        when(staffRepository.findByAccountId("acc-1"))
                .thenReturn(Optional.of(staff));

        // when
        String token = tokenService.generateToken(account);

        // then
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateToken_customerAccount_success() {
        // given
        Account account = new Account();
        account.setId("cust-1");
        account.setAccountType(AccountType.CUSTOMER);

        // when
        String token = tokenService.generateToken(account);

        // then
        assertNotNull(token);
    }

}
