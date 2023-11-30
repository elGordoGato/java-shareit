package ru.practicum.shareit.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private final Long id;

    private String name;

    private String email;
}
