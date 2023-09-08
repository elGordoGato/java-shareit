package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Builder
public class CommentDto {
    private final Long id;

    @NotBlank
    private final String text;

    private final String authorName;

    private final LocalDateTime created;
}
