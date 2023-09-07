package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class BookingRequest {
    private final LocalDateTime start;

    private final LocalDateTime end;

    private final Long itemId;
}
