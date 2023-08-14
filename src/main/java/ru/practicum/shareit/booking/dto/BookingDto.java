package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class BookingDto {
    private final Long id;

    @NotBlank(message = "Необходимо указать start date")
    private final LocalDate start;

    @NotBlank(message = "Необходимо указать end date")
    private final LocalDate end;

    private Status status;

    private final User booker;

    private final Item item;
}
