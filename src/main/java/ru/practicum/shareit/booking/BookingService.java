package ru.practicum.shareit.booking;

import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.state.State;

import java.util.List;


public interface BookingService {

    BookingDto create(BookingRequest bookingRequest, long bookerId)
            throws NoSuchMethodException, MethodArgumentNotValidException;

    BookingDto approve(Long bookingId, long userId, boolean approved)
            throws NoSuchMethodException, MethodArgumentNotValidException;

    BookingDto getById(long bookingId, long userId);

    List<BookingDto> getAllForUserByState(long userId, State state, boolean isBooker);
}
