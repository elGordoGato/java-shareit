package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> findAll();

    User add(User user);


    User update(User targetUser, User user);

    Optional<User> findById(Long userId);

    int deleteById(Long userId);
}
