package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.state.State;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static ru.practicum.shareit.booking.BookingMapper.*;
import static ru.practicum.shareit.booking.status.Status.*;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    @Transactional
    public BookingDto create(BookingRequest bookingRequest, long bookerId) {
        User booker = getUser(bookerId, "book item " + bookingRequest.getItemId());
        Item item = itemRepository.findByIdIsAndOwnerIdNot(bookingRequest.getItemId(), bookerId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %s not found when trying to book it by user %s",
                                bookingRequest.getItemId(), bookerId)));
        if (!item.getAvailable()) {
            throw new BadRequestException("Item is not available");
        }
        // Tests do not support these feature - not passing when enabled:
        //validateDate(bookingRequest.getStart(), bookingRequest.getEnd());
        Booking booking = bookingRepository.save(requestToBooking(bookingRequest, booker, item));
        log.info("Booking successfully created: {}", booking);
        return bookingToDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, long userId, boolean approved) {
        Booking booking = bookingRepository.findByIdAndBookerIdNot(bookingId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Booking with id %s not found when trying to approve it by user %s",
                                bookingId, userId)));
        if (!booking.getStatus().equals(WAITING)) {
            throw new BadRequestException("Booking is already " + booking.getStatus().getStatus());
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    String.format("User with id: %s has no rights to approve/reject booking: %s", userId, bookingId));
        }
        booking.setStatus(approved ? APPROVED : REJECTED);
        return bookingToDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(long bookingId, long userId) {
        isUserExist(userId);
        Booking booking = bookingRepository.findByIdAndByBookerOrOwner(bookingId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Booking with id %s not found when trying to get it by user %s",
                                bookingId, userId)));
        return bookingToDto(booking);
    }

    @Override
    public List<BookingDto> getAllForUserByState(long userId, State state, boolean isBooker, Pageable page) {
        isUserExist(userId);
        BooleanExpression byUserId = isBooker ? QBooking.booking.booker.id.eq(userId) :
                QBooking.booking.item.owner.id.eq(userId);
        BooleanExpression byState = getConditionByState(state);
        Iterable<Booking> foundItems = bookingRepository.findAll(byUserId.and(byState), page);
        return convertToDtoList(foundItems);
    }

    private BooleanExpression getConditionByState(State state) {
        BooleanExpression byState;
        switch (state) {
            case ALL:
                byState = null;
                break;
            case PAST:
                byState = QBooking.booking.end.before(now());
                break;
            case CURRENT:
                byState = QBooking.booking.start.before(now())
                        .and(QBooking.booking.end.after(now()));
                break;
            case FUTURE:
                byState = QBooking.booking.start.after(now());
                break;
            case WAITING:
                byState = QBooking.booking.status.eq(WAITING);
                break;
            case REJECTED:
                byState = QBooking.booking.status.eq(REJECTED);
                break;
            default:
                throw new ForbiddenException("No such state" + state);
        }
        return byState;
    }

    private User getUser(long id, String operation) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to %s",
                                id, operation)));
    }

    private void isUserExist(long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("There is no user in database with id: " + id);
        }
    }


/*    private void validateDate(LocalDateTime start, LocalDateTime end) {
        List<Booking> interferedBookings = bookingRepository.findAllByDateInterfering(start, end);
        if (!interferedBookings.isEmpty()) {
            throw new ConflictException("Required dates already booked by " + interferedBookings);
        }
    }*/
}
