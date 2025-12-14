package com.theatermgnt.theatermgnt.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.customer.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByAccountId(String accountId);

    boolean existsByAccountUsername(String username);

    boolean existsByAccountEmail(String email);

    boolean existsByAccountId(String accountId);
}
