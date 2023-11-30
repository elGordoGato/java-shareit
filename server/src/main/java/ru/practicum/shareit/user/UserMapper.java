package ru.practicum.shareit.user;

public class UserMapper {
    public static User dtoToUser(UserDto dto) {
        return new User(dto.getId(), dto.getName(), dto.getEmail());
    }

    public static UserDto userToDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

}
