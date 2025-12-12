package com.theatermgnt.theatermgnt.seatType.entity;

import java.math.BigDecimal;

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
@Table(name = "seatTypes")
@SQLDelete(sql = "UPDATE seatTypes SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class SeatType extends BaseEntity {

    String typeName;
    BigDecimal basePriceModifier;
}
