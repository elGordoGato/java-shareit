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
    public List<UserDto> getAllUsers() {
        log.info("Received request to get all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        log.info("Received request to get user with id: {}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    public UserDto saveNewUser(@RequestBody @Valid UserDto user) {
        log.info("Received request to create user: {}", user);
        return userService.saveUser(user);
    }

    @PatchMapping("/{userId}")
    public UserDto updateExistedUser(@PathVariable Long userId, @RequestBody UserDto user) {
        log.info("Received request to update user: {}", user);
        return userService.updateUser(userId, user);
    }

    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        log.info("Received request to delete user with id: {}", userId);
        userService.deleteUserById(userId);
    }
}
