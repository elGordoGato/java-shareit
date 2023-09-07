package ru.practicum.shareit.booking;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.state.State;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.status.Status.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;


    @Test
    void create_shouldSaveBooking_whenValidRequestAndBookerAndItem() {
        // given
        User booker = new User(1L, "booker", "booker@gmail.com");
        Item item = new Item(1L,
                "item",
                "description",
                true,
                new User(2L, "owner", "owner@gmail.com"),
                null);
        Booking booking = getBooking(booker, item);
        BookingRequest bookingRequest = BookingRequest.builder()
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(1L)
                .build();

        when(userService.findById(anyLong()))
                .thenReturn(booker);
        when(itemRepository.findByIdIsAndOwnerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // when
        BookingDto result = bookingService.create(bookingRequest, booker.getId());

        // then
        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(WAITING, result.getStatus());

        verify(userService, times(1))
                .findById(booker.getId());
        verify(itemRepository, times(1))
                .findByIdIsAndOwnerIdNot(item.getId(), booker.getId());
        verify(bookingRepository, times(1))
                .save(any(Booking.class));
    }

    @Test
    void create_shouldThrowNotFoundException_whenInvalidBookerId() {
        // given
        BookingRequest bookingRequest = BookingRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();
        long bookerId = 1L;

        when(userService.findById(anyLong())).thenThrow(new NotFoundException(
                "User with id 1 not found when trying to book item 1"));

        // when
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingRequest, bookerId));

        // then
        assertEquals("User with id 1 not found when trying to book item 1",
                exception.getMessage());

        verify(userService).findById(bookerId);
        verifyNoInteractions(itemRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_shouldThrowNotFoundException_whenInvalidItemId() {
        // given
        BookingRequest bookingRequest = BookingRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();
        User booker = new User(1L, "booker", "booker@gmail.com");

        when(userService.findById(anyLong()))
                .thenReturn(booker);
        when(itemRepository.findByIdIsAndOwnerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(bookingRequest, booker.getId()));

        // then
        assertEquals("Item with id 1 not found when trying to book it by user 1",
                exception.getMessage());

        verify(userService).findById(booker.getId());
        verify(itemRepository).findByIdIsAndOwnerIdNot(bookingRequest.getItemId(), booker.getId());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void create_shouldThrowBadRequestException_whenItemIsNotAvailable() {
        // given
        BookingRequest bookingRequest = BookingRequest.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();
        User booker = new User(1L, "booker", "booker@gmail.com");
        Item item = new Item(1L,
                "item",
                "description",
                false,
                new User(2L, "owner", "owner@gmail.com"),
                null);

        when(userService.findById(anyLong()))
                .thenReturn(booker);
        when(itemRepository.findByIdIsAndOwnerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.of(item));

// when
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.create(bookingRequest, booker.getId()));

// then
        assertEquals("Item is not available", exception.getMessage());

        verify(userService).findById(booker.getId());
        verify(itemRepository).findByIdIsAndOwnerIdNot(bookingRequest.getItemId(), booker.getId());
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void approve_shouldUpdateBookingStatus_whenValidBookingIdAndUserIdAndApproved() {
        // given
        long bookingId = 1L;
        long userId = 2L;
        boolean approved = true;
        User booker = new User(1L, "booker", "booker@gmail.com");
        User owner = new User(2L, "owner", "owner@gmail.com");
        Item item = new Item(1L, "item", "description", true, owner, null);
        Booking booking = getBooking(booker, item);

        when(bookingRepository.findByIdAndBookerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // when
        BookingDto result = bookingService.approve(bookingId, userId, approved);

        // then
        assertNotNull(result);
        assertEquals(APPROVED, result.getStatus());

        verify(bookingRepository).findByIdAndBookerIdNot(bookingId, userId);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approve_shouldUpdateBookingStatus_whenValidBookingIdAndUserIdAndRejected() {
        // given
        long bookingId = 1L;
        long userId = 2L;
        boolean approved = false;
        User booker = new User(1L, "booker", "booker@gmail.com");
        User owner = new User(2L, "owner", "owner@gmail.com");
        Item item = new Item(1L, "item", "description", true, owner, null);
        Booking booking = getBooking(booker, item);

        when(bookingRepository.findByIdAndBookerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        // when
        BookingDto result = bookingService.approve(bookingId, userId, approved);

        // then
        assertNotNull(result);
        assertEquals(REJECTED, result.getStatus());

        verify(bookingRepository).findByIdAndBookerIdNot(bookingId, userId);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approve_shouldThrowNotFoundException_whenInvalidBookingId() {
        // given
        long bookingId = 1L;
        long userId = 2L;

        when(bookingRepository.findByIdAndBookerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

// when
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approve(bookingId, userId, true));

// then
        assertEquals("Booking with id 1 not found when trying to approve it by user 2",
                exception.getMessage());

        verify(bookingRepository).findByIdAndBookerIdNot(bookingId, userId);
        verifyNoInteractions(userService);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void approve_shouldThrowBadRequestException_whenBookingIsNotWaiting() {
// given
        long bookingId = 1L;
        long userId = 2L;
        User booker = new User(1L, "booker", "booker@gmail.com");
        User owner = new User(2L, "owner", "owner@gmail.com");
        Item item = new Item(1L, "item", "description", true, owner, null);
        Booking booking = getBooking(booker, item);
        booking.setStatus(APPROVED);

        when(bookingRepository.findByIdAndBookerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));

// when
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.approve(bookingId, userId, true));

// then
        assertEquals("Booking is already Подтверждено", exception.getMessage());

        verify(bookingRepository).findByIdAndBookerIdNot(bookingId, userId);
        verifyNoInteractions(userService);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void approve_shouldThrowForbiddenException_whenUserIsNotOwnerOfItem() {
// given
        long bookingId = 1L;
        long userId = 3L;
        User booker = new User(1L, "booker", "booker@gmail.com");
        User owner = new User(2L, "owner", "owner@gmail.com");
        Item item = new Item(1L, "item", "description", true, owner, null);
        Booking booking = getBooking(booker, item);

        when(bookingRepository.findByIdAndBookerIdNot(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));

// when
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> bookingService.approve(bookingId, userId, true));

// then
        assertEquals("User with id: 3 has no rights to approve/reject booking: 1",
                exception.getMessage());

        verify(bookingRepository).findByIdAndBookerIdNot(bookingId, userId);
        verifyNoInteractions(userService);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void getById_shouldReturnBookingDto_whenValidBookingIdAndUserId() {
// given
        long bookingId = 1L;
        long userId = 1L;
        User booker = new User(1L, "booker", "booker@gmail.com");
        User owner = new User(2L, "owner", "owner@gmail.com");
        Item item = new Item(1L, "item", "description", true, owner, null);
        Booking booking = getBooking(booker, item);

        when(bookingRepository.findByIdAndByBookerOrOwner(anyLong(), anyLong()))
                .thenReturn(Optional.of(booking));

// when
        BookingDto result = bookingService.getById(bookingId, userId);

// then
        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(WAITING, result.getStatus());

        verify(userService).existsById(userId);
        verify(bookingRepository).findByIdAndByBookerOrOwner(bookingId, userId);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenInvalidBookingId() {
// given
        long bookingId = 1L;
        long userId = 1L;

        when(bookingRepository.findByIdAndByBookerOrOwner(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

// when
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(bookingId, userId));

// then
        assertEquals("Booking with id 1 not found when trying to get it by user 1",
                exception.getMessage());

        verify(userService).existsById(userId);
        verify(bookingRepository).findByIdAndByBookerOrOwner(bookingId, userId);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenInvalidUserId() {
// given
        long bookingId = 1L;
        long userId = 1L;

        doThrow(new NotFoundException("There is no user in database with id: 1"))
                .when(userService).existsById(userId);

// when
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getById(bookingId, userId));

// then
        assertEquals("There is no user in database with id: 1", exception.getMessage());

        verify(userService).existsById(userId);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getAllForUserByState_should_return_list_of_booking_dto_when_user_and_state_exist() {
        // Arrange
        long userId = 1L;
        State state = State.CURRENT;
        boolean isBooker = true;
        User booker = new User(1L, "booker", "booker@gmail.com");
        User owner = new User(2L, "owner", "owner@gmail.com");
        Item item = new Item(1L, "item", "description", true, owner, null);
        Booking booking1 = getBooking(booker, item);
        Booking booking2 = getBooking(booker, item);
        Pageable page = Pageable.unpaged();
        Page<Booking> bookings = new PageImpl<>(List.of(booking1, booking2));
        List<BookingDto> expected = BookingMapper.convertToDtoList(bookings);

        when(bookingRepository.findAll(any(BooleanExpression.class), eq(page)))
                .thenReturn(bookings); // Return the dummy bookings

        // Act
        List<BookingDto> actual = bookingService.getAllForUserByState(userId, state, isBooker, page);

        // Assert
        assertEquals(expected, actual); // Check if the actual result matches the expected result
    }

    @Test
    void getAllForUserByState_should_throw_not_found_exception_when_user_does_not_exist() {
        // Arrange
        long userId = 1L;
        State state = State.CURRENT;
        boolean isBooker = true;
        Pageable page = Pageable.unpaged();

        // Stub the method of the mocked dependency
        doThrow(new NotFoundException("There is no user in database with id: 1"))
                .when(userService).existsById(userId); // Assume the user does not exist

        // Act and Assert
        assertThrows(NotFoundException.class, () -> {
            bookingService.getAllForUserByState(userId, state, isBooker, page); // Expect a NotFoundException to be thrown
        });
    }

    private Booking getBooking(User booker, Item item) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setStatus(WAITING);
        return booking;
    }

}