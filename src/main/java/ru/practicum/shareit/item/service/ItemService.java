package ru.practicum.shareit.item.service;

import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto item, long userId);

    ItemDto update(Long itemId, long userId, ItemDto itemDto);

    ItemDto getById(long itemId, long userId);

    List<ItemDto> getAllForUser(long userId);

    List<ItemDto> searchByNameAndDescr(String text, long userId);

    CommentDto create(CommentDto comment, long itemId, long userId)
            throws NoSuchMethodException, MethodArgumentNotValidException;
}
