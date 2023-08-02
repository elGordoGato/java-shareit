package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUserById(Long userId);

    UserDto saveUser(UserDto user);

    UserDto updateUser(Long userId, UserDto user);

    void deleteUserById(Long userId);
}
