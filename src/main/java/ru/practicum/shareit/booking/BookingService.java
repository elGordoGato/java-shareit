package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.state.State;

import java.util.List;


public interface BookingService {

    BookingDto create(BookingRequest bookingRequest, long bookerId);

    BookingDto approve(Long bookingId, long userId, boolean approved);

    BookingDto getById(long bookingId, long userId);

    List<BookingDto> getAllForUserByState(long userId, State state, boolean isBooker, Pageable page);
}
