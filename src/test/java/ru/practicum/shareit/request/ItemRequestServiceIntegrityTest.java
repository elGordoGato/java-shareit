package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrityTest {

    private final EntityManager em;
    private final ItemRequestService service;

    @AfterEach
    void resetIds() {
        em.createNativeQuery("ALTER TABLE requests ALTER COLUMN id RESTART WITH 1; " +
                        " ALTER TABLE users ALTER COLUMN id RESTART WITH 1; " +
                        " ALTER TABLE items ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
    }

    @Test
    void create() {
        addUser(getUser(1));
        ItemRequestDto request1 = ItemRequestDto.builder().description("Request for table").build();

        ItemRequestDto createdRequest = service.create(1L, request1);

        List<ItemRequest> allRequests = em.createQuery("SELECT r FROM ItemRequest r ", ItemRequest.class)
                .getResultList();
        assertThat(allRequests, hasSize(1));
        assertThat(allRequests.get(0).getId(), equalTo(1L));
        assertThat(allRequests.get(0).getDescription(), equalTo("Request for table"));
        assertThat(allRequests.get(0).getCreated(), equalTo(createdRequest.getCreated()));
        assertThat(createdRequest.getId(), equalTo(1L));
        assertThat(createdRequest.getDescription(), equalTo("Request for table"));
        assertThat(createdRequest.getCreated(), instanceOf(LocalDateTime.class));
        assertThat(createdRequest.getItems(), hasSize(0));

    }

    @Test
    void findAllByUserId() {
        setupTwoUsersWithItemsAndRequests();

        List<ItemRequestDto> foundRequests = service.findAllByUserId(1L);
        assertThat(foundRequests, hasSize(1));
        assertThat(foundRequests.get(0).getId(), equalTo(1L));
        assertThat(foundRequests.get(0).getDescription(), equalTo("Request for table"));
        assertThat(foundRequests.get(0).getCreated(), instanceOf(LocalDateTime.class));
        assertThat(foundRequests.get(0).getItems(), hasSize(1));
        assertThat(foundRequests.get(0).getItems().get(0),
                hasProperty("name", equalTo("Table")));
    }

    @Test
    void findAll() {
        setupTwoUsersWithItemsAndRequests();

        List<ItemRequestDto> foundRequests = service.findAll(1L, Pageable.unpaged());
        assertThat(foundRequests, hasSize(1));
        assertThat(foundRequests.get(0).getId(), equalTo(2L));
        assertThat(foundRequests.get(0).getDescription(), equalTo("Request for chair"));
        assertThat(foundRequests.get(0).getCreated(), instanceOf(LocalDateTime.class));
        assertThat(foundRequests.get(0).getItems(), hasSize(1));
        assertThat(foundRequests.get(0).getItems().get(0),
                hasProperty("name", equalTo("Chair")));
    }

    @Test
    void findById() {
        setupTwoUsersWithItemsAndRequests();

        ItemRequestDto foundRequest = service.findById(1L, 2L);

        assertThat(foundRequest, notNullValue());
        assertThat(foundRequest.getId(), equalTo(2L));
        assertThat(foundRequest.getDescription(), equalTo("Request for chair"));
        assertThat(foundRequest.getCreated(), instanceOf(LocalDateTime.class));
        assertThat(foundRequest.getItems(), hasSize(1));
        assertThat(foundRequest.getItems().get(0),
                hasProperty("name", equalTo("Chair")));
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

    private void addItem(int itemId, User owner, ItemRequest request) {
        switch (itemId) {
            case 1:
                em.persist(new Item(
                        null, "Table", "Table description", true, owner, request));
                break;
            case 2:
                em.persist(new Item(
                        null, "Chair", "Chair description", true, owner, request));
        }
    }

    private void setupTwoUsersWithItemsAndRequests() {
        User userWithChair = addUser(getUser(1));
        ItemRequest tableRequest = addRequest(1, userWithChair);
        User userWithTable = addUser((getUser(2)));
        ItemRequest chairRequest = addRequest(2, userWithTable);
        addItem(1, userWithTable, tableRequest);
        addItem(2, userWithChair, chairRequest);
    }
}