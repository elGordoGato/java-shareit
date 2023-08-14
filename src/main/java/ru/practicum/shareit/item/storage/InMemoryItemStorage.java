package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import ru.practicum.shareit.item.model.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.List;
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
    public Item add(Item item) {
        item.setId(Optional.ofNullable(item.getId())
                .orElseGet(() -> {
                    while (itemStorage.keySet().stream()
                            .anyMatch(it -> it.getId().equals(idCounter))) {
                        idCounter++;
                    }
                    return idCounter++;
                }));
        itemStorage.put(item, 0);
        return item;
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return itemStorage.keySet().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Item update(Item targetItem, Item newItem) {
        Optional.ofNullable(newItem.getName())
                .ifPresent(targetItem::setName);
        Optional.ofNullable(newItem.getDescription())
                .ifPresent(targetItem::setDescription);
        Optional.ofNullable(newItem.getAvailable())
                .ifPresent(targetItem::setAvailable);
        return targetItem;
    }

    @Override
    public int findRentCount(Item item) {
        return itemStorage.get(item);
    }

    @Override
    public Map<Item, Integer> findAllItemsByUser(User user) {
        return itemStorage.keySet().stream()
                .filter(item -> item.getOwner().equals(user))
                .collect(Collectors.toMap(Function.identity(), itemStorage::get));
    }

    @Override
    public Map<Item, Integer> findByNameAndDescr(String text, User user) {
        return itemStorage.keySet().stream().filter(
                        item -> item.getName().toLowerCase().contains(text)
                                || item.getDescription().toLowerCase().contains(text)
                                && item.getAvailable())
                .collect(Collectors.toMap(Function.identity(), itemStorage::get));
    }

    @Override
    public List<Item> findAll() {
        return null;
    }

    @Override
    public List<Item> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Item> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Item> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(Item entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Item> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Item> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Item> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Item> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Item> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Item> entities) {

    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Item getOne(Long aLong) {
        return null;
    }

    @Override
    public Item getById(Long aLong) {
        return null;
    }

    @Override
    public Item getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Item> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Item> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Item> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Item> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Item> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Item> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Item, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }
}
