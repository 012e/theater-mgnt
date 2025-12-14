package com.theatermgnt.theatermgnt.screening.dto.response;

import com.theatermgnt.theatermgnt.common.enums.RoomType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScreeningResponse {
     String id;
     String movieName;
     String roomName;
     LocalDateTime startTime;
     LocalDateTime endTime;
     String status;
}
