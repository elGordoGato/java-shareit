package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    public static Comment dtoToComment(CommentDto dto, Item item, User author) {
        return new Comment(dto.getId(), dto.getText(), item, author);

    }

    public static CommentDto commentToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static List<CommentDto> commentToDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::commentToDto)
                .collect(Collectors.toList());
    }
}

