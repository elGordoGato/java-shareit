package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Map;
import java.util.Optional;

public interface ItemStorage {
    Item add(Item item);

    Optional<Item> findById(Long itemId);

    Item update(Item targetItem, Item item);

    int findRentCount(Item item);

    Map<Item, Integer> findAllItemsByUser(User user);

    Map<Item, Integer> findByNameAndDescr(String text, User user);
}
