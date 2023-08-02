package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserStorage userRepository;
    private final ItemStorage itemRepository;

    @Override
    public ItemDto addItem(ItemDto item, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Данный пользователь не найден"));
        return ItemMapper.itemToDto(itemRepository.saveItem(ItemMapper.dtoToItem(item, user)), 0);
    }

    @Override
    public ItemDto updateItem(Long itemId, long userId, ItemDto itemDto) {
        Item targetItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Данный товар не найден"));
        if (!targetItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    String.format("У пользователя %s нет прав редактирровать товар %s", userId, itemId));
        }
        log.info(String.valueOf(targetItem.toString().hashCode()));
        int rentCount = itemRepository.findRentCount(targetItem);
        return ItemMapper.itemToDto(itemRepository.update(
                        targetItem, ItemMapper.dtoToItem(itemDto, targetItem.getOwner())),
                rentCount);
    }

    @Override
    public ItemDto getItemById(long itemId, long userId) {
        Item targetItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Данный товар не найден"));
        return ItemMapper.itemToDto(targetItem, itemRepository.findRentCount(targetItem));
    }

    @Override
    public List<ItemDto> getAllItems(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Данный пользователь не найден"));
        Map<Item, Integer> foundItems = itemRepository.findAllItemsByUser(user);
        return foundItems.keySet().stream().map(item -> ItemMapper.itemToDto(item, foundItems.get(item)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItemByNameAndDescr(String text, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Данный пользователь не найден"));
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        Map<Item, Integer> foundItems = itemRepository.findItemsByNameAndDescr(text.toLowerCase(), user);
        return foundItems.keySet().stream().map(item -> ItemMapper.itemToDto(item, foundItems.get(item)))
                .collect(Collectors.toList());
    }
}
