package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(ItemDto item, long userId) {
        User creatingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to create item: %s", userId, item)));
        Item itemCreated = itemRepository.save(
                ItemMapper.dtoToItem(item, creatingUser));
        log.info("Item created: {}", itemCreated);
        return ItemMapper.itemToDto(itemCreated, null, null);
    }

    @Override
    public ItemDto update(Long itemId, long userId, ItemDto itemDto) {
        Item targetItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %s not found when trying to update it", itemId)));
        User owner = targetItem.getOwner();
        if (!owner.getId().equals(userId)) {
            throw new ForbiddenException(
                    String.format("У пользователя %s нет прав редактирровать товар %s", userId, itemId));
        }
        Optional.ofNullable(itemDto.getName())
                .ifPresent(targetItem::setName);
        Optional.ofNullable(itemDto.getDescription())
                .ifPresent(targetItem::setDescription);
        Optional.ofNullable(itemDto.getAvailable())
                .ifPresent(targetItem::setAvailable);
        targetItem = itemRepository.save(targetItem);
        log.info("Item with ID: {} updated - new data: {}", itemId, targetItem);
        List<BookingsByItem> bookingsByItemList = bookingRepository.findDatesByItemId(
                List.of(itemId), LocalDateTime.now());
        BookingsByItem dateByItem = bookingsByItemList.stream().findAny().orElse(null);
        List<Comment> comments = commentRepository.findAllByItemIdIn(Collections.singletonList(itemId));
        return ItemMapper.itemToDto(targetItem, dateByItem, comments);
    }

    @Override
    public ItemDto getById(long itemId, long userId) {
        Item targetItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %s not found when trying to get it", itemId)));
        log.info("Item found: {}", targetItem);
        BookingsByItem dateByItem;
        if (targetItem.getOwner().getId().equals(userId)) {
            List<BookingsByItem> bookingsByItemList = bookingRepository.findDatesByItemId(
                    Collections.singletonList(itemId), LocalDateTime.now());
            dateByItem = bookingsByItemList.stream().findAny().orElse(null);
        } else {
            dateByItem = null;
        }
        List<Comment> comments = commentRepository.findAllByItemIdIn(Collections.singletonList(itemId));
        return ItemMapper.itemToDto(targetItem, dateByItem, comments);
    }

    @Override
    public List<ItemDto> getAllForUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to get his items", userId)));
        Map<Long, Item> itemMap = itemRepository.findAllByOwnerId(user.getId()).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        log.info("Found {} items of user with id: {}", itemMap.size(), userId);
        Map<Long, BookingsByItem> bookingMap = bookingRepository.findDatesByItemId(
                new ArrayList<>(itemMap.keySet()), LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(BookingsByItem::getItemId, Function.identity()));
        Map<Long, List<Comment>> commentMap = commentRepository.findAllByItemIdIn(new ArrayList<>(itemMap.keySet()))
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        return itemMap.values().stream()
                .map(item -> ItemMapper.itemToDto(
                        item,
                        bookingMap.get(item.getId()), commentMap.get(item.getId()))
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchByNameAndDescr(String text, long userId) {
        if (!userRepository.existsById(userId)) {
                 throw new NotFoundException(
                        String.format("User with id %s not found when trying to search for item by %s", userId, text));
        }
        if (text.isBlank()) {
            log.info("No text for search entered");
            return Collections.emptyList();
        }
        Map<Long, Item> foundItems = itemRepository
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(text, text)
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        log.info("Found {} items containing {}", foundItems.size(), text);
        Map<Long, List<Comment>> commentMap = commentRepository.findAllByItemIdIn(new ArrayList<>(foundItems.keySet()))
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        return foundItems.values().stream()
                .map(item -> ItemMapper.itemToDto(item, null, commentMap.get(item.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto create(CommentDto comment, long itemId, long userId)
            throws NoSuchMethodException, MethodArgumentNotValidException {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to create comment: %s", userId, comment)));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException(
                String.format("Item with id %s not found when trying to comment it", itemId)));
        if (!bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, LocalDateTime.now(), Status.APPROVED)) {
            new BadRequestException(comment, "comment",
                    String.format("User with id: %s did not rent item with id: %s to comment it", userId, itemId),
                    this.getClass().getMethod("create", CommentDto.class, long.class, long.class));
        }
        Comment commentCreated = commentRepository.save(
                CommentMapper.dtoToComment(comment, item, author));
        log.info("Comment created: {}", commentCreated);
        return CommentMapper.commentToDto(commentCreated);
    }
}
