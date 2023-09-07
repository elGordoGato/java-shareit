package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.item.dto.ItemShort;
import ru.practicum.shareit.user.UserId;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class BookingDto {
    private final Long id;

    private final LocalDateTime start;

    private final LocalDateTime end;

    private final UserId booker;

    private final ItemShort item;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Status status;
}
