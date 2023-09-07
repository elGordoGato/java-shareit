package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    public void testGetAll() {
        // Mock the repository response
        User user1 = new User(1L, "John", "john@example.com");
        User user2 = new User(2L, "Jane", "jane@example.com");
        when(repository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // Call the service method
        List<UserDto> result = userService.getAll();

        // Verify the result
        verify(repository, Mockito.times(1))
                .findAll();
        assertThat(2, equalTo(result.size()));
        assertThat("John", equalTo(result.get(0).getName()));
        assertThat("jane@example.com", equalTo(result.get(1).getEmail()));
    }

    @Test
    public void testGetById_ExistingUser() {
        // Mock the repository response
        User user = new User(1L, "John", "john@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        // Call the service method
        UserDto result = userService.getById(1L);

        // Verify the result
        verify(repository, Mockito.times(1))
                .findById(Mockito.anyLong());
        assertThat("John", equalTo(result.getName()));
        assertThat("john@example.com", equalTo(result.getEmail()));
    }

    @Test
    public void testGetById_NonExistingUser() {
        // Mock the repository response
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Verify that NotFoundException is thrown
        assertThrows(NotFoundException.class, () -> userService.getById(1L));
        verify(repository, Mockito.times(1))
                .findById(Mockito.anyLong());
    }

    @Test
    public void testCreate() {
        // Mock the repository response
        UserDto userDto = new UserDto(null, "John", "john@example.com");
        User createdUser = new User(1L, "John", "john@example.com");
        when(repository.save(Mockito.any(User.class))).thenReturn(createdUser);

        // Call the service method
        UserDto result = userService.create(userDto);

        // Verify the result
        verify(repository, Mockito.times(1))
                .save(Mockito.any());
        assertThat(1L, equalTo(result.getId()));
        assertThat("John", equalTo(result.getName()));
        assertThat("john@example.com", equalTo(result.getEmail()));
    }

    @Test
    public void testUpdate_ExistingUser() {
        // Mock the repository response
        User existingUser = new User(1L, "John", "john@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(existingUser));

        // Create a new user data
        UserDto newData = new UserDto(null, "Jane", "jane@example.com");

        // Call the service method
        UserDto result = userService.update(1L, newData);

        // Verify the result
        assertEquals("Jane", result.getName());
        assertEquals("jane@example.com", result.getEmail());
    }

    @Test
    public void testUpdate_NonExistingUser() {
        // Mock the repository response
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Create a new user data
        UserDto newData = new UserDto(null, "Jane", "jane@example.com");

        // Verify that NotFoundException is thrown
        assertThrows(NotFoundException.class, () -> userService.update(1L, newData));
    }


    @Test
    public void testDeleteById_ExistingUser() {
        // Mock the repository response
        when(repository.existsById(1L)).thenReturn(true);

        // Call the service method
        userService.deleteById(1L);

        // Verify that deleteById method is called with correct argument
        verify(repository, Mockito.times(1))
                .deleteById(Mockito.anyLong());
        verify(repository).deleteById(1L);
    }

    @Test
    public void testDeleteById_NonExistingUser() {
        // Mock the repository response
        when(repository.existsById(1L)).thenReturn(false);

        // Verify that NotFoundException is thrown
        assertThrows(NotFoundException.class, () -> userService.deleteById(1L));
        verify(repository, Mockito.times(0))
                .deleteById(Mockito.anyLong());
    }
}