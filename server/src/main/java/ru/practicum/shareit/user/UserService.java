package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    List<UserDto> getAll();

    UserDto getById(Long userId);

    UserDto create(UserDto user);

    UserDto update(Long userId, UserDto user);

    void deleteById(Long userId);

    User findById(long userId);

    void existsById(long userId);
}
