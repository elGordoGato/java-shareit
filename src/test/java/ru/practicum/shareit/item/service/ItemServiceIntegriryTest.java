package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.booking.status.Status.APPROVED;

@Slf4j
@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegriryTest {

    private final EntityManager em;
    private final ItemService service;

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
        addUser(getUser(1));
        ItemDto receivedItemDto = ItemDto.builder().name("Table")
                .description("Descr for table")
                .available(true)
                .build();

        ItemDto createdItem = service.create(receivedItemDto, 1L);

        List<Item> allItems = em.createQuery("SELECT i FROM Item i ", Item.class)
                .getResultList();
        assertThat(allItems, hasSize(1));
        assertThat(allItems.get(0).getId(), equalTo(1L));
        assertThat(allItems.get(0).getName(), equalTo("Table"));
        assertThat(allItems.get(0).getDescription(), equalTo("Descr for table"));
        assertThat(allItems.get(0).getAvailable(), equalTo(true));
        assertThat(createdItem.getId(), equalTo(allItems.get(0).getId()));
        assertThat(createdItem.getDescription(), equalTo(allItems.get(0).getDescription()));
        assertThat(createdItem.getAvailable(), equalTo(allItems.get(0).getAvailable()));
        assertThat(createdItem.getLastBooking(), nullValue());
        assertThat(createdItem.getRequestId(), nullValue());
        assertThat(createdItem.getRentCounter(), nullValue());
    }

    @Test
    void update() {
        User owner = addUser(getUser(1));
        User anothetUser = addUser(getUser(2));
        ItemDto receivedItemDto = ItemDto.builder().name("Updated Table")
                .description("Updated Descr for table")
                .available(false)
                .build();
        ItemRequest request = addRequest(1, anothetUser);
        addItem(1, owner, request);

        ItemDto updatedItem = service.update(1L, owner.getId(), receivedItemDto);

        List<Item> allItems = em.createQuery("SELECT i FROM Item i ", Item.class)
                .getResultList();
        assertThat(allItems, hasSize(1));
        assertThat(allItems.get(0).getId(), equalTo(1L));
        assertThat(allItems.get(0).getName(), equalTo(receivedItemDto.getName()));
        assertThat(allItems.get(0).getDescription(), equalTo(receivedItemDto.getDescription()));
        assertThat(allItems.get(0).getAvailable(), equalTo(receivedItemDto.getAvailable()));
        assertThat(updatedItem.getId(), equalTo(allItems.get(0).getId()));
        assertThat(updatedItem.getDescription(), equalTo(allItems.get(0).getDescription()));
        assertThat(updatedItem.getAvailable(), equalTo(allItems.get(0).getAvailable()));
        assertThat(updatedItem.getLastBooking(), nullValue());
        assertThat(updatedItem.getRequestId(), equalTo(request.getId()));
        assertThat(updatedItem.getRentCounter(), nullValue());
    }

    @Test
    void getById() {
        User owner = addUser(getUser(1));
        User anothetUser = addUser(getUser(2));
        ItemRequest request = addRequest(1, anothetUser);
        addItem(1, owner, request);

        ItemDto foundItem = service.getById(1L, anothetUser.getId());

        assertThat(foundItem.getId(), equalTo(1L));
        assertThat(foundItem.getName(), equalTo("Table"));
        assertThat(foundItem.getDescription(), equalTo("Table description"));
        assertThat(foundItem.getAvailable(), equalTo(true));
        assertThat(foundItem.getLastBooking(), nullValue());
        assertThat(foundItem.getRequestId(), equalTo(request.getId()));
        assertThat(foundItem.getRentCounter(), nullValue());
    }

    @Test
    void getAllForUser() {
        User owner = addUser(getUser(1));
        User anothetUser = addUser(getUser(2));
        ItemRequest request = addRequest(1, anothetUser);
        addItem(1, owner, request);
        addItem(2, anothetUser, null);

        List<ItemDto> foundItems = service.getAllForUser(owner.getId(), Pageable.unpaged());

        assertThat(foundItems, hasSize(1));
        assertThat(foundItems.get(0).getId(), equalTo(1L));
        assertThat(foundItems.get(0).getName(), equalTo("Table"));
        assertThat(foundItems.get(0).getDescription(), equalTo("Table description"));
        assertThat(foundItems.get(0).getAvailable(), equalTo(true));
        assertThat(foundItems.get(0).getLastBooking(), nullValue());
        assertThat(foundItems.get(0).getRequestId(), equalTo(request.getId()));
        assertThat(foundItems.get(0).getRentCounter(), nullValue());
    }

    @Test
    void searchByNameAndDescr() {
        setupTwoUsersWithItemsAndRequests();

        List<ItemDto> foundItems = service.searchByNameAndDescr("AblE", 2L, Pageable.unpaged());

        assertThat(foundItems, hasSize(1));
        assertThat(foundItems.get(0).getId(), equalTo(1L));
        assertThat(foundItems.get(0).getName(), equalTo("Table"));
        assertThat(foundItems.get(0).getDescription(), equalTo("Table description"));
        assertThat(foundItems.get(0).getAvailable(), equalTo(true));
        assertThat(foundItems.get(0).getLastBooking(), nullValue());
        assertThat(foundItems.get(0).getRequestId(), equalTo(1L));
        assertThat(foundItems.get(0).getRentCounter(), nullValue());
    }

    @Test
    void testCreateComment() {
        setupTwoUsersWithItemsAndRequests();
        CommentDto receivedDto = CommentDto.builder().text("Comment Test").build();

        CommentDto createdComment = service.create(receivedDto, 1L, 1L);

        assertThat(createdComment, notNullValue());
        assertThat(createdComment, hasProperty("id", equalTo(1L)));
        assertThat(createdComment, hasProperty("text", equalTo("Comment Test")));
        assertThat(createdComment, hasProperty("created", notNullValue()));
        assertThat(createdComment, hasProperty("authorName", equalTo("John")));
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
        Item item1 = addItem(1, userWithTable, tableRequest);
        Item item2 = addItem(2, userWithChair, chairRequest);
        Booking lastBooking = new Booking();
        lastBooking.setStart(now().minusDays(1));
        lastBooking.setEnd(now().minusHours(1));
        lastBooking.setStatus(APPROVED);
        lastBooking.setItem(item1);
        lastBooking.setBooker(userWithChair);
        em.persist(lastBooking);
    }
}