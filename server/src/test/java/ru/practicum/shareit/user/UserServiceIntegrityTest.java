package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@Slf4j
@Transactional
@SpringBootTest(
        properties = "db.name = test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrityTest {

    private final EntityManager em;
    private final UserService service;

    @AfterEach
    void resetIds() {
        em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
    }

    @Test
    void getAll() {
        addUser(2);

        List<UserDto> userList = service.getAll();

        assertThat(userList, hasSize(2));
        assertThat(userList.get(0).getId(), equalTo(1L));
        assertThat(userList.get(0).getName(), equalTo("John"));
        assertThat(userList.get(0).getEmail(), equalTo("john@example.com"));
        assertThat(userList.get(1).getName(), equalTo("Jane"));
        assertThat(userList.get(1).getEmail(), equalTo("jane@example.com"));
    }

    @Test
    void getById() {
        addUser(2);

        UserDto userDto = service.getById(1L);

        assertThat(userDto.getId(), equalTo(1L));
        assertThat(userDto.getName(), equalTo("John"));
        assertThat(userDto.getEmail(), equalTo("john@example.com"));

    }

    @Test
    void create() {
        UserDto user1 = new UserDto(null, "John", "john@example.com");

        UserDto createdUser = service.create(user1);

        List<User> allUsers = em.createQuery("SELECT u FROM User u ", User.class).getResultList();
        assertThat(allUsers, hasSize(1));
        assertThat(allUsers, hasItem(new User(1L, "John", "john@example.com")));
        assertThat(createdUser.getId(), equalTo(1L));
        assertThat(createdUser.getName(), equalTo("John"));
        assertThat(createdUser.getEmail(), equalTo("john@example.com"));
    }

    @Test
    void update() {
        addUser(1);

        UserDto updatedUser = service.update(1L, new UserDto(
                1L, "Jane", "jane@example.com"));

        List<User> allUsers = em.createQuery("SELECT u FROM User u ", User.class).getResultList();
        assertThat(allUsers.size(), equalTo(1));
        assertThat(allUsers, hasItem(new User(1L, "Jane", "jane@example.com")));
        assertThat(updatedUser.getId(), equalTo(1L));
        assertThat(updatedUser.getName(), equalTo("Jane"));
        assertThat(updatedUser.getEmail(), equalTo("jane@example.com"));
    }

    @Test
    void deleteById() {
        addUser(1);

        service.deleteById(1L);

        List<User> allUsers = em.createQuery("SELECT u FROM User u ", User.class).getResultList();
        assertThat(allUsers, empty());
    }

    private void addUser(int quantity) {
        switch (quantity) {
            case 1:
                em.persist(new User(null, "John", "john@example.com"));
                break;
            case 2:
                em.persist(new User(null, "John", "john@example.com"));
                em.persist(new User(null, "Jane", "jane@example.com"));
        }
    }
}