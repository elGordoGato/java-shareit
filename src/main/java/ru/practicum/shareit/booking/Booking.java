package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class Booking {
    private final Long id;

    private final LocalDate start;

    private final LocalDate end;

    private Status status;

    private final User booker;

    private final Item item;
}
