package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {
    public static ItemRequest dtoToItemRequest(ItemRequestDto requestDto, User owner) {
        return new ItemRequest(requestDto.getId(), requestDto.getDescription(), owner);
    }

    public static ItemRequestDto itemRequestToDto(ItemRequest request, List<Item> itemList) {
        List<ItemDto> itemDtos = itemList != null ?
                itemList.stream()
                        .map(item -> ItemMapper.itemToDto(item, null, null))
                        .collect(Collectors.toList()) :
                Collections.emptyList();
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }
}
