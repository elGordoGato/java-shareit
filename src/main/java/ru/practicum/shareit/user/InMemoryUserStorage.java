package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final List<User> storedUsers = new ArrayList<>();
    private long idCounter = 1;

    @Override
    public List<User> findAll() {
        return storedUsers;
    }

    @Override
    public User add(User user) {
        checkEmailForExistence(user.getEmail());
        user.setId(Optional.ofNullable(user.getId())
                .orElseGet(() -> {
                    while (storedUsers.stream()
                            .anyMatch(u -> u.getId().equals(idCounter))) {
                        idCounter++;
                    }
                    return idCounter++;
                }));
        storedUsers.add(user);
        return user;
    }

    @Override
    public User update(User targetUser, User user) {
        Optional.ofNullable(user.getName()).ifPresent(targetUser::setName);
        Optional.ofNullable(user.getEmail()).ifPresent(e -> {
            if (!targetUser.getEmail().equals(user.getEmail())) {
                checkEmailForExistence(user.getEmail());
                targetUser.setEmail(e);
            }
        });
        return targetUser;
    }

    @Override
    public Optional<User> findById(Long userId) {
        return storedUsers.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    @Override
    public int deleteById(Long userId) {
        return findById(userId).map(user -> {
            storedUsers.remove(user);
            return 1;
        }).orElse(0);
    }

    private void checkEmailForExistence(String email) {
        if (storedUsers.stream()
                .anyMatch(u -> u.getEmail().equals(email))) {
            throw new ConflictException(
                    String.format("Адрес электронной почты: %s уже занят", email));
        }

    }
}
