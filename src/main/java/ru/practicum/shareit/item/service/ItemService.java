package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto item, long userId);

    ItemDto updateItem(Long itemId, long userId, ItemDto itemDto);

    ItemDto getItemById(long itemId, long userId);

    List<ItemDto> getAllItems(long userId);

    List<ItemDto> searchItemByNameAndDescr(String text, long userId);
}
