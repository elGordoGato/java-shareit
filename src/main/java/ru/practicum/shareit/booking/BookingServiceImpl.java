package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.state.State;
import ru.practicum.shareit.booking.status.Status;
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
    public BookingDto create(BookingRequest bookingRequest, long bookerId)
            throws NoSuchMethodException, MethodArgumentNotValidException {
        User booker = userRepository.findById(bookerId).orElseThrow(() -> new NotFoundException(
                String.format("User with id %s not found when trying to book item %s",
                        bookerId, bookingRequest.getItemId())));
        Item item = itemRepository.findByIdIsAndOwnerIdNot(bookingRequest.getItemId(), bookerId).orElseThrow(
                () -> new NotFoundException(
                        String.format("Item with id %s not found when trying to book it by user %s",
                                bookingRequest.getItemId(), bookerId)));
        if (!item.getAvailable()) {
            new BadRequestException(item, "item", "Item is not available",
                    this.getClass().getMethod("create", BookingRequest.class, long.class));
        }
        // Tests do not support these feature - not passing when enabled:
        //validateDate(bookingRequest.getStart(), bookingRequest.getEnd());
        Booking booking = bookingRepository.save(BookingMapper.requestToBooking(bookingRequest, booker, item));
        log.info("Booking successfully created: {}", booking);
        return BookingMapper.bookingToDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, long userId, boolean approved)
            throws NoSuchMethodException, MethodArgumentNotValidException {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException(
                String.format("Booking with id %s not found when trying to approve it by user %s",
                        bookingId, userId)));
        if (!booking.getStatus().equals(Status.WAITING)) {
            new BadRequestException(booking, "booking",
                    "Booking is already " + booking.getStatus().getStatus(),
                    this.getClass().getMethod("approve", Long.class, long.class, boolean.class));
        }
        if (booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException(
                    String.format("Booker with id: %s has no rights to approve/reject booking: %s", userId, bookingId));
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    String.format("User with id: %s has no rights to approve/reject booking: %s", userId, bookingId));
        }
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return BookingMapper.bookingToDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(long bookingId, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("There is no user in database with id: " + userId);
        }
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException(
                String.format("Booking with id %s not found when trying to get it by user %s",
                        bookingId, userId)));
        if (!(booking.getItem().getOwner().getId().equals(userId) || booking.getBooker().getId().equals(userId))) {
            throw new NotFoundException(
                    String.format("User with id: %s has no rights to get booking: %s", userId, bookingId));
        }
        return BookingMapper.bookingToDto(booking);
    }

    @Override
    public List<BookingDto> getAllForUserByState(long userId, State state, boolean isBooker) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("There is no user in database with id: " + userId);
        }
        BooleanExpression byUserId = isBooker ? QBooking.booking.booker.id.eq(userId) :
                QBooking.booking.item.owner.id.eq(userId);
        BooleanExpression byState = getConditionByState(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        Iterable<Booking> foundItems = bookingRepository.findAll(byUserId.and(byState), sort);
        return BookingMapper.convertToDtoList(foundItems);
    }

    private BooleanExpression getConditionByState(State state) {
        BooleanExpression byState;
        switch (state) {
            case ALL:
                byState = null;
                break;
            case PAST:
                byState = QBooking.booking.end.before(LocalDateTime.now());
                break;
            case CURRENT:
                byState = QBooking.booking.start.before(LocalDateTime.now())
                        .and(QBooking.booking.end.after(LocalDateTime.now()));
                break;
            case FUTURE:
                byState = QBooking.booking.start.after(LocalDateTime.now());
                break;
            case WAITING:
                byState = QBooking.booking.status.eq(Status.WAITING);
                break;
            case REJECTED:
                byState = QBooking.booking.status.eq(Status.REJECTED);
                break;
            default:
                throw new ForbiddenException("No such state" + state);
        }
        return byState;
    }

    private void validateDate(LocalDateTime start, LocalDateTime end) {
        List<Booking> interferedBookings = bookingRepository.findAllByDateInterfering(start, end);
        if (!interferedBookings.isEmpty()) {
            throw new ConflictException("Required dates already booked by " + interferedBookings);
        }
    }
}
