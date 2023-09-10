package ru.practicum.shareit.booking.dto;

import lombok.Getter;
import lombok.ToString;
import ru.practicum.shareit.booking.Booking;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;

@Getter
@ToString
public class BookingsByItem {
    private final Long itemId;

    private final Booking lastBooking;

    private final Booking nextBooking;

    private final Long rentCounter;

    public BookingsByItem(Long itemId, Booking lastBooking, Booking nextBooking, Long rentCounter) {
        this.itemId = itemId;
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
        this.rentCounter = rentCounter;
    }

    public BookingsByItem(Long itemId, Booking lastBooking, Booking nextBooking) {
        this.itemId = itemId;
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
        this.rentCounter = 0L;
    }
}
