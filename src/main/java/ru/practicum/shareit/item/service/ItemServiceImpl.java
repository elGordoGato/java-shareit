package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
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
    public ItemDto create(ItemDto item, long userId) {
        User creatingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to create item: %s", userId, item)));
        Item itemCreated = itemRepository.add(
                ItemMapper.dtoToItem(item, creatingUser));
        log.info("Item created: {}", itemCreated);
        return ItemMapper.itemToDto(itemCreated, 0);
    }

    @Override
    public ItemDto update(Long itemId, long userId, ItemDto itemDto) {
        Item targetItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %s not found when trying to update it", itemId)));
        if (!targetItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    String.format("У пользователя %s нет прав редактирровать товар %s", userId, itemId));
        }
        int rentCount = itemRepository.findRentCount(targetItem);
        Item updated = itemRepository.update(targetItem, ItemMapper.dtoToItem(itemDto, targetItem.getOwner()));
        log.info("Item with ID: {} updated - new data: {}", itemId, updated);
        return ItemMapper.itemToDto(updated, rentCount);
    }

    @Override
    public ItemDto getById(long itemId, long userId) {
        Item targetItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %s not found when trying to get it", itemId)));
        log.info("Item found: {}", targetItem);
        return ItemMapper.itemToDto(targetItem, itemRepository.findRentCount(targetItem));
    }

    @Override
    public List<ItemDto> getAllForUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to get his items", userId)));
        Map<Item, Integer> foundItems = itemRepository.findAllItemsByUser(user);
        log.info("Found {} items of user with id: {}", foundItems.size(), userId);
        return foundItems.keySet().stream()
                .map(item -> ItemMapper.itemToDto(item, foundItems.get(item)))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchByNameAndDescr(String text, long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to search for item by %s", userId, text)));
        if (text.isBlank()) {
            log.info("No text for search entered");
            return Collections.emptyList();
        }
        Map<Item, Integer> foundItems = itemRepository.findByNameAndDescr(text.toLowerCase(), user);
        log.info("Found {} items containing {}", foundItems.size(), text);
        return foundItems.keySet().stream()
                .map(item -> ItemMapper.itemToDto(item, foundItems.get(item)))
                .collect(Collectors.toList());
    }
}
