package com.theatermgnt.theatermgnt.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    boolean existsByTransactionId(String transactionId);
    Optional<Payment> findByBookingId(String bookingId);
}

