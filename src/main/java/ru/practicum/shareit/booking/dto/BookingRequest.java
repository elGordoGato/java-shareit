package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.exception.DateOrder;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
@DateOrder
public class BookingRequest {
    @FutureOrPresent
    @NotNull
    private final LocalDateTime start;

    @NotNull
    private final LocalDateTime end;

    @NotNull
    private final Long itemId;
}
