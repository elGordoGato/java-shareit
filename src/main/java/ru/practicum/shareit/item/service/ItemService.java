package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto item, long userId);

    ItemDto update(Long itemId, long userId, ItemDto itemDto);

    ItemDto getById(long itemId, long userId);

    List<ItemDto> getAllForUser(long userId);

    List<ItemDto> searchByNameAndDescr(String text, long userId);
}
