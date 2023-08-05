package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage repository;

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(UserMapper::userToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long userId) {
        User foundedUser = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to update it", userId)));
        log.info("User with ID {} found: {}", userId, foundedUser);
        return UserMapper.userToDto(foundedUser);
    }

    @Override
    public UserDto create(UserDto user) {
        User createdUser = repository.add(
                UserMapper.dtoToUser(user));
        log.info("User created: {}", createdUser);
        return UserMapper.userToDto(createdUser);
    }

    @Override
    public UserDto update(Long userId, UserDto user) {
        User updatedUser = repository.update(repository.findById(userId)
                        .orElseThrow(() -> new NotFoundException(
                                String.format("User with id %s not found when trying to update it", userId))),
                UserMapper.dtoToUser(user));
        log.info("User with ID {} updated - new data: {}", userId, updatedUser);
        return UserMapper.userToDto(updatedUser);
    }

    @Override
    public void deleteById(Long userId) {
        if (repository.deleteById(userId) < 1) {
            throw new NotFoundException(
                    String.format("User with id %s not found when trying to update it", userId));
        }
        log.info("User with ID {} successfully deleted", userId);
    }


}
