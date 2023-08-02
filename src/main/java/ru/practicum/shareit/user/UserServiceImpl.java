package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage repository;

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream().map(UserMapper::userToDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long userId) {
        return UserMapper.userToDto(
                repository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("Данный пользователь не найден")));
    }

    @Override
    public UserDto saveUser(UserDto user) {
        return UserMapper.userToDto(
                repository.save(
                        UserMapper.dtoToUser(user)));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto user) {
        return UserMapper.userToDto(repository.update(repository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("Данный пользователь не найден")),
                UserMapper.dtoToUser(user)));
    }

    @Override
    public void deleteUserById(Long userId) {
        if (repository.deleteUserById(userId) < 1) {
            throw new NotFoundException("Данный пользователь не найден");
        }
    }


}
