package com.theatermgnt.theatermgnt.authorization.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.authorization.cinema.entity.Cinema;

public interface CinemaRepository extends JpaRepository<Cinema, String> {
    boolean existsByName(String name);
}
