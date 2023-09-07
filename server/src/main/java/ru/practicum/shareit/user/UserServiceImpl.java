package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(UserMapper::userToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(Long userId) {
        User foundedUser = findById(userId);
        log.info("User with ID {} found: {}", userId, foundedUser);
        return UserMapper.userToDto(foundedUser);
    }

    @Override
    @Transactional
    public UserDto create(UserDto user) {
        User createdUser = repository.save(
                UserMapper.dtoToUser(user));
        log.info("User created: {}", createdUser);
        return UserMapper.userToDto(createdUser);
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto withNewData) {
        User toBeUpdated = findById(userId);
        Optional.ofNullable(withNewData.getName()).ifPresent(toBeUpdated::setName);
        Optional.ofNullable(withNewData.getEmail()).ifPresent(toBeUpdated::setEmail);
        log.info("User with ID {} updated - new data: {}", userId, toBeUpdated);
        return UserMapper.userToDto(toBeUpdated);
    }

    @Override
    @Transactional
    public void deleteById(Long userId) {
        existsById(userId);
        repository.deleteById(userId);
        log.info("User with ID {} successfully deleted", userId);
    }

    @Override
    public User findById(long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to get it", userId)));
    }

    @Override
    public void existsById(long userId) {
        if (!repository.existsById(userId)) {
            throw new NotFoundException(
                    String.format("User with id %s not found when trying to delete it", userId));
        }
    }


}
