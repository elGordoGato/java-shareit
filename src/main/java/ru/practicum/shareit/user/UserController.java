package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Received request to get all users");
        return userService.getAll();
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        log.info("Received request to get user with id: {}", userId);
        return userService.getById(userId);
    }

    @PostMapping
    public UserDto create(@RequestBody @Valid UserDto user) {
        log.info("Received request to create user: {}", user);
        return userService.create(user);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto user) {
        log.info("Received request to update user with ID: {} - new data: {}", userId, user);
        return userService.update(userId, user);
    }

    @DeleteMapping("/{userId}")
    public void deleteById(@PathVariable Long userId) {
        log.info("Received request to delete user with id: {}", userId);
        userService.deleteById(userId);
    }
}
