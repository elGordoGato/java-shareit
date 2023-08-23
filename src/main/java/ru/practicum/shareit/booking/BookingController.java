package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.state.State;
import ru.practicum.shareit.exception.StateValidator;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */


@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;


    @PostMapping
    public BookingDto create(@RequestBody @Valid BookingRequest bookingRequest,
                             @RequestHeader("X-Sharer-User-Id") long bookerId) throws MethodArgumentNotValidException,
            NoSuchMethodException {
        log.info("Received request from user with id: {} to book item: {} from {} to {}",
                bookerId, bookingRequest.getItemId(), bookingRequest.getStart(), bookingRequest.getEnd());
        return bookingService.create(bookingRequest, bookerId);
    }


    @PatchMapping("/{bookingId}")
    public BookingDto approve(@PathVariable Long bookingId,
                              @RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestParam boolean approved)
            throws MethodArgumentNotValidException, NoSuchMethodException {
        log.info("Received request from user with Id {} to " +
                (approved ? "approve" : "reject") +
                " booking with id: {}", userId, bookingId);
        return bookingService.approve(bookingId, userId, approved);
    }


    @GetMapping("/{bookingId}")
    public BookingDto getById(@PathVariable long bookingId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to get booking with id: {}", userId, bookingId);
        return bookingService.getById(bookingId, userId);
    }


    @GetMapping
    public List<BookingDto> getAllForBooker(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @RequestParam(defaultValue = "ALL") String state) {
        State stateEnum = StateValidator.checkState(state);
        log.info("Арендатор {} запросил предоставить {} его бронирования", userId, stateEnum.getState());
        return bookingService.getAllForUserByState(userId, stateEnum, true);
    }


    @GetMapping("/owner")
    public List<BookingDto> getAllForOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestParam(defaultValue = "ALL") String state) {
        State stateEnum = StateValidator.checkState(state);
        log.info("Владелец {} запросил предоставить {} бронирования его вещей", userId, stateEnum.getState());
        return bookingService.getAllForUserByState(userId, stateEnum, false);
    }
}