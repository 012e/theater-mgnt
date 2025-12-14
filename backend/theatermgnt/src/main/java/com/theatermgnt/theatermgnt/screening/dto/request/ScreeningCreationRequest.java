package com.theatermgnt.theatermgnt.screening.dto.request;


import com.theatermgnt.theatermgnt.common.enums.RoomType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScreeningCreationRequest {
    @NotNull
    String roomId;

    @NotNull
    String movieId;

    @NotNull
    LocalDateTime startTime;

    @NotNull
    LocalDateTime endTime;
}
