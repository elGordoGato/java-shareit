package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingShort {
    private final Long id;

    private final LocalDateTime start;

    private final LocalDateTime end;

    private final Long bookerId;

    private Status status;

}
