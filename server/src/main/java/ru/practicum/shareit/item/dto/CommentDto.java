package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentDto {
    private final Long id;

    private final String text;

    private final String authorName;

    private final LocalDateTime created;
}
