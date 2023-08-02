package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Item, Integer> itemStorage = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Item saveItem(Item item) {
        item.setId(Optional.ofNullable(item.getId()).orElseGet(() -> {
            while (itemStorage.keySet().stream().anyMatch(it -> it.getId().equals(idCounter))) {
                idCounter++;
            }
            return idCounter++;
        }));
        itemStorage.put(item, 0);
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return itemStorage.keySet().stream().filter(item -> item.getId().equals(itemId)).findFirst();
    }

    @Override
    public Item update(Item targetItem, Item newItem) {
        log.info("Value before update " + itemStorage.keySet().hashCode());
        Optional.ofNullable(newItem.getName()).ifPresent(targetItem::setName);
        Optional.ofNullable(newItem.getDescription()).ifPresent(targetItem::setDescription);
        Optional.ofNullable(newItem.getAvailable()).ifPresent(targetItem::setAvailable);
        log.info("Value after update " + itemStorage.keySet().hashCode());
        log.info(itemStorage.toString());
        return targetItem;
    }

    @Override
    public int findRentCount(Item item) {
        log.info("start");
        Item target = itemStorage.keySet().stream().filter(item1 -> item1.getId().equals(1L)).findFirst().orElseThrow(() -> new NotFoundException("yq"));
        log.info(target.hashCode() + target.toString());
        log.info(itemStorage.toString());
        log.info(String.valueOf(itemStorage.keySet().hashCode()));
        log.info(String.valueOf(itemStorage.containsKey(target)));
        log.info("End with: " + itemStorage.get(target));
        log.info("Received to get: " + item.hashCode());
        log.info(String.valueOf(itemStorage.keySet().hashCode()));
        log.info(itemStorage.toString());
        return itemStorage.get(item);
    }

    @Override
    public Map<Item, Integer> findAllItemsByUser(User user) {
        return itemStorage.keySet().stream().filter(item -> item.getOwner().equals(user))
                .collect(Collectors.toMap(Function.identity(), itemStorage::get));
    }

    @Override
    public Map<Item, Integer> findItemsByNameAndDescr(String text, User user) {
        return itemStorage.keySet().stream().filter(
                        item -> item.getName().toLowerCase().contains(text)
                                || item.getDescription().toLowerCase().contains(text)
                                && item.getAvailable())
                .collect(Collectors.toMap(Function.identity(), itemStorage::get));
    }
}
