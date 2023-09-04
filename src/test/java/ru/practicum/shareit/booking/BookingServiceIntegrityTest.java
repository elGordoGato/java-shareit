package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.state.State;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.practicum.shareit.booking.status.Status.*;

@Slf4j
@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrityTest {

    private final EntityManager em;
    private final BookingService service;

    @AfterEach
    void resetIds() {
        em.createNativeQuery("ALTER TABLE requests ALTER COLUMN id RESTART WITH 1; " +
                        " ALTER TABLE users ALTER COLUMN id RESTART WITH 1; " +
                        " ALTER TABLE items ALTER COLUMN id RESTART WITH 1; " +
                        " ALTER TABLE bookings ALTER COLUMN id RESTART WITH 1;")
                .executeUpdate();
    }

    @Test
    void create() {
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        Item table = addItem(1, userWithTable, tableRequest);
        Item chair = addItem(2, userWithChair, chairRequest);
        BookingRequest requestForTable = BookingRequest.builder()
                .itemId(table.getId())
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        BookingDto createdBooking = service.create(requestForTable, userWithChair.getId());

        List<Booking> allBookings = em.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();
        assertThat(allBookings, hasSize(1));
        assertThat(allBookings.get(0).getId(), equalTo(createdBooking.getId()));
        assertThat(allBookings.get(0).getStart(), equalTo(createdBooking.getStart()));
        assertThat(allBookings.get(0).getEnd(), equalTo(createdBooking.getEnd()));
        assertThat(allBookings.get(0).getStatus(), equalTo(createdBooking.getStatus()));
        assertThat(allBookings.get(0).getBooker().getId(), equalTo(createdBooking.getBooker().getId()));
        assertThat(allBookings.get(0).getItem().getId(), equalTo(createdBooking.getItem().getId()));
        assertThat(createdBooking.getId(), equalTo(1L));
        assertThat(createdBooking.getStart(), equalTo(requestForTable.getStart()));
        assertThat(createdBooking.getEnd(), equalTo(requestForTable.getEnd()));
        assertThat(createdBooking.getStatus(), equalTo(WAITING));
        assertThat(createdBooking.getBooker().getId(), equalTo(userWithChair.getId()));
        assertThat(createdBooking.getItem().getId(), equalTo(table.getId()));
    }

    @Test
    void approve() {
        setupTwoUsersWithItemsAndRequests();

        BookingDto approvedBooking = service.approve(1L, 2L, true);

        List<Booking> allBookings = em.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();
        assertThat(allBookings, hasSize(1));
        assertThat(allBookings.get(0).getId(), equalTo(approvedBooking.getId()));
        assertThat(allBookings.get(0).getStart(), equalTo(approvedBooking.getStart()));
        assertThat(allBookings.get(0).getEnd(), equalTo(approvedBooking.getEnd()));
        assertThat(allBookings.get(0).getStatus(), equalTo(approvedBooking.getStatus()));
        assertThat(allBookings.get(0).getBooker().getId(), equalTo(approvedBooking.getBooker().getId()));
        assertThat(allBookings.get(0).getItem().getId(), equalTo(approvedBooking.getItem().getId()));
        assertThat(approvedBooking.getId(), equalTo(1L));
        assertThat(approvedBooking.getStatus(), equalTo(APPROVED));
    }

    @Test
    void getById() {
        setupTwoUsersWithItemsAndRequests();

        BookingDto foundBooking = service.getById(1L, 2L);

        assertThat(foundBooking.getId(), equalTo(1L));
        assertTrue(foundBooking.getStart().isAfter(LocalDateTime.now().plusMinutes(59)));
        assertTrue(foundBooking.getEnd().isBefore(LocalDateTime.now().plusDays(1)));
        assertThat(foundBooking.getStatus(), equalTo(WAITING));
        assertThat(foundBooking.getBooker().getId(), equalTo(1L));
        assertThat(foundBooking.getItem().getId(), equalTo(1L));
    }

    @Test
    void getAllForUserByState() {
        Sort sort = Sort.by(DESC, "start");
        Pageable page = PageRequest.of(0, 10, sort);
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        Item table = addItem(1, userWithTable, tableRequest);
        Item chair = addItem(2, userWithChair, chairRequest);
        Booking bookingForTableFuture = new Booking();
        bookingForTableFuture.setStart(now().plusHours(1));
        bookingForTableFuture.setEnd(now().plusDays(1));
        bookingForTableFuture.setStatus(WAITING);
        bookingForTableFuture.setItem(table);
        bookingForTableFuture.setBooker(userWithChair);
        em.persist(bookingForTableFuture);
        Booking bookingForTablePast = new Booking();
        bookingForTablePast.setStart(now().minusDays(1));
        bookingForTablePast.setEnd(now().minusHours(1));
        bookingForTablePast.setStatus(APPROVED);
        bookingForTablePast.setItem(table);
        bookingForTablePast.setBooker(userWithChair);
        em.persist(bookingForTablePast);
        Booking bookingForTablePastRejected = new Booking();
        bookingForTablePastRejected.setStart(now().minusDays(5));
        bookingForTablePastRejected.setEnd(now().minusHours(2));
        bookingForTablePastRejected.setStatus(REJECTED);
        bookingForTablePastRejected.setItem(table);
        bookingForTablePastRejected.setBooker(userWithChair);
        em.persist(bookingForTablePastRejected);

        List<BookingDto> foundBookingsPast = service.getAllForUserByState(
                userWithChair.getId(), State.PAST, true, page);
        List<BookingDto> foundBookingsWaiting = service.getAllForUserByState(
                userWithTable.getId(), State.WAITING, false, page);

        assertThat(foundBookingsPast, hasSize(2));
        assertThat(foundBookingsPast.get(0).getId(), equalTo(bookingForTablePast.getId()));
        assertThat(foundBookingsPast.get(0).getStart(), equalTo(bookingForTablePast.getStart()));
        assertThat(foundBookingsPast.get(0).getEnd(), equalTo(bookingForTablePast.getEnd()));
        assertThat(foundBookingsPast.get(0).getStatus(), equalTo(bookingForTablePast.getStatus()));
        assertThat(foundBookingsPast.get(0).getBooker().getId(), equalTo(1L));
        assertThat(foundBookingsPast.get(0).getItem().getId(), equalTo(1L));
        assertThat(foundBookingsPast.get(1).getId(), equalTo(bookingForTablePastRejected.getId()));
        assertThat(foundBookingsPast.get(1).getStatus(), equalTo(bookingForTablePastRejected.getStatus()));
        assertThat(foundBookingsPast.get(1).getStart(), equalTo(bookingForTablePastRejected.getStart()));
        assertThat(foundBookingsWaiting, hasSize(1));
        assertThat(foundBookingsWaiting.get(0).getId(), equalTo(bookingForTableFuture.getId()));
        assertThat(foundBookingsWaiting.get(0).getStatus(), equalTo(bookingForTableFuture.getStatus()));
    }

    private User getUser(int userId) {
        switch (userId) {
            case 1:
                return new User(null, "John", "john@example.com");
            case 2:
                return new User(null, "Jane", "jane@example.com");
            default:
                return new User();
        }
    }

    private User addUser(User user) {
        em.persist(user);
        return user;
    }

    private ItemRequest addRequest(int requestId, User author) {
        ItemRequest request = new ItemRequest();
        switch (requestId) {
            case 1:
                request = new ItemRequest(null, "Request for table", author);
                em.persist(request);
                break;
            case 2:
                request = new ItemRequest(null, "Request for chair", author);
                em.persist(request);
                break;
        }
        return request;
    }

    private Item addItem(int itemId, User owner, ItemRequest request) {
        Item item = new Item();
        switch (itemId) {
            case 1:
                item = new Item(null, "Table", "Table description", true, owner, request);
                em.persist(item);
                break;
            case 2:
                item = new Item(null, "Chair", "Chair description", true, owner, request);
                em.persist(item);
                break;
        }
        return item;
    }

    private void setupTwoUsersWithItemsAndRequests() {
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        Item table = addItem(1, userWithTable, tableRequest);
        Item chair = addItem(2, userWithChair, chairRequest);
        Booking bookingForTable = new Booking();
        bookingForTable.setStart(now().plusHours(1));
        bookingForTable.setEnd(now().plusDays(1));
        bookingForTable.setStatus(WAITING);
        bookingForTable.setItem(table);
        bookingForTable.setBooker(userWithChair);
        em.persist(bookingForTable);
    }
}