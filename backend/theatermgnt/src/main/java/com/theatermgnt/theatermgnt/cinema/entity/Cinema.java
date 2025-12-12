package com.theatermgnt.theatermgnt.cinema.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cinemas")
@SQLDelete(sql = "UPDATE cinemas SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Cinema extends BaseEntity {
    String name;
    String address;
    String city;
    String phoneNumber;
    String managerId;
}
