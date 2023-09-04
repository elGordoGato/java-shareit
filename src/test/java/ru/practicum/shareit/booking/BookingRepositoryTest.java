package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.booking.status.Status.*;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    public void contextLoads() {
        assertThat(em, notNullValue());
    }

    @Test
    void verifyBootstrappingByPersistingAnBooking() {
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        Item table = addItem(1, userWithTable, tableRequest);
        Booking bookingForTable = new Booking();
        bookingForTable.setStart(now().plusHours(1));
        bookingForTable.setEnd(now().plusDays(1));
        bookingForTable.setStatus(WAITING);
        bookingForTable.setItem(table);
        bookingForTable.setBooker(userWithChair);

        assertThat(bookingForTable.getId(), nullValue());
        em.persist(bookingForTable);
        assertThat(bookingForTable.getId(), notNullValue());
    }

    @Test
    void findAllByDateInterfering() {
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        Item table = addItem(1, userWithTable, tableRequest);
        Item chair = addItem(2, userWithChair, chairRequest);
        Booking bookingForTableFuture = getBookingForTableFutureWaiting(table, userWithChair);
        Booking bookingForTablePast = getBookingForTablePastApproved(table, userWithChair);
        Booking bookingForTablePastRejected = getBookingForTablePastRejected(table, userWithChair);

        List<Booking> interferingBookings = bookingRepository.findAllByDateInterfering(
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusHours(12));

        assertThat(interferingBookings, hasSize(1));
        assertThat(interferingBookings.get(0).getId(), equalTo(bookingForTablePast.getId()));
        assertThat(interferingBookings.get(0).getStart(), equalTo(bookingForTablePast.getStart()));
        assertThat(interferingBookings.get(0).getEnd(), equalTo(bookingForTablePast.getEnd()));
        assertThat(interferingBookings.get(0).getStatus(), equalTo(bookingForTablePast.getStatus()));
        assertThat(interferingBookings.get(0).getBooker().getId(), equalTo(userWithChair.getId()));
        assertThat(interferingBookings.get(0).getItem().getId(), equalTo(table.getId()));
    }

    @Test
    void findDatesByItemId() {
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        Item table = addItem(1, userWithTable, tableRequest);
        Item chair = addItem(2, userWithChair, chairRequest);
        Booking bookingForTableFuture = getBookingForTableFutureWaiting(table, userWithChair);
        Booking bookingForTablePast = getBookingForTablePastApproved(table, userWithChair);
        Booking bookingForTablePastRejected = getBookingForTablePastRejected(table, userWithChair);

        List<BookingsByItem> bookingsByItem = bookingRepository.findDatesByItemId(List.of(table.getId()), now());
        assertThat(bookingsByItem, hasSize(1));
        assertThat(bookingsByItem.get(0).getItemId(), equalTo(table.getId()));
        assertThat(bookingsByItem.get(0).getLastBooking(), equalTo(bookingForTablePast));
        assertThat(bookingsByItem.get(0).getNextBooking(), equalTo(bookingForTableFuture));
        assertThat(bookingsByItem.get(0).getRentCounter(), equalTo(1L));
    }

    @Test
    void findByIdAndByBookerOrOwner() {
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        Item table = addItem(1, userWithTable, tableRequest);
        Item chair = addItem(2, userWithChair, chairRequest);
        Booking bookingForTableFuture = getBookingForTableFutureWaiting(table, userWithChair);
        Booking bookingForTablePast = getBookingForTablePastApproved(table, userWithChair);
        Booking bookingForTablePastRejected = getBookingForTablePastRejected(table, userWithChair);

        Optional<Booking> foundBookingByBooker = bookingRepository.findByIdAndByBookerOrOwner(
                bookingForTablePastRejected.getId(), userWithChair.getId());
        Optional<Booking> foundBookingByOwner = bookingRepository.findByIdAndByBookerOrOwner(
                bookingForTablePastRejected.getId(), userWithTable.getId());

        assertTrue(foundBookingByOwner.isPresent());
        assertTrue(foundBookingByBooker.isPresent());
        assertThat(foundBookingByOwner.get(), equalTo(foundBookingByBooker.get()));
        assertThat(foundBookingByOwner.get(), equalTo(bookingForTablePastRejected));
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

    private Booking getBookingForTableFutureWaiting(Item table, User userWithChair) {
        Booking bookingForTableFuture = new Booking();
        bookingForTableFuture.setStart(now().plusHours(1));
        bookingForTableFuture.setEnd(now().plusDays(1));
        bookingForTableFuture.setStatus(WAITING);
        bookingForTableFuture.setItem(table);
        bookingForTableFuture.setBooker(userWithChair);
        em.persist(bookingForTableFuture);
        return bookingForTableFuture;
    }

    private Booking getBookingForTablePastApproved(Item table, User userWithChair) {
        Booking bookingForTablePast = new Booking();
        bookingForTablePast.setStart(now().minusDays(1));
        bookingForTablePast.setEnd(now().minusHours(1));
        bookingForTablePast.setStatus(APPROVED);
        bookingForTablePast.setItem(table);
        bookingForTablePast.setBooker(userWithChair);
        em.persist(bookingForTablePast);
        return bookingForTablePast;
    }

    private Booking getBookingForTablePastRejected(Item table, User userWithChair) {
        Booking bookingForTablePastRejected = new Booking();
        bookingForTablePastRejected.setStart(now().minusDays(5));
        bookingForTablePastRejected.setEnd(now().minusHours(2));
        bookingForTablePastRejected.setStatus(REJECTED);
        bookingForTablePastRejected.setItem(table);
        bookingForTablePastRejected.setBooker(userWithChair);
        em.persist(bookingForTablePastRejected);
        return bookingForTablePastRejected;
    }

}