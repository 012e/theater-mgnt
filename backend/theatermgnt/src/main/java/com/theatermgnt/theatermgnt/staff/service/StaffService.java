package com.theatermgnt.theatermgnt.staff.service;

import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.staff.dto.request.SearchStaffRequest;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffAccountCreationRequest;
import com.theatermgnt.theatermgnt.staff.dto.request.StaffProfileUpdateRequest;
import com.theatermgnt.theatermgnt.staff.dto.response.StaffResponse;
import com.theatermgnt.theatermgnt.staff.entity.Staff;

public interface StaffService {
    @Transactional
    //    @PreAuthorize("hasRole('ADMIN')")
    Staff createStaffProfile(StaffAccountCreationRequest request, Account account, Set<Role> roles);

    //    @PreAuthorize("hasRole('ADMIN')")
    List<StaffResponse> getAll();

    //    @PreAuthorize("hasRole('ADMIN')")
    StaffResponse getStaffProfile(String staffId);

    StaffResponse getMyInfo();

    //    /// SEARCH STAFF BY NAME/EMAIL/PHONE
    List<StaffResponse> searchStaff(SearchStaffRequest request);

    StaffResponse updateStaffProfile(String staffId, StaffProfileUpdateRequest request);

    void deleteStaff(String staffId);
}
