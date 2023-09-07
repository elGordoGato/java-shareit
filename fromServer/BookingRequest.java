package ru.practicum.shareit.fromServer;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
public class BookingRequest {
    @FutureOrPresent
    @NotNull
    private final LocalDateTime start;

    @NotNull
    private final LocalDateTime end;

    @NotNull
    private final Long itemId;
}
