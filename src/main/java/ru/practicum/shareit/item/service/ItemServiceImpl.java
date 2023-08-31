package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static ru.practicum.shareit.booking.status.Status.APPROVED;
import static ru.practicum.shareit.item.dto.CommentMapper.commentToDto;
import static ru.practicum.shareit.item.dto.CommentMapper.dtoToComment;
import static ru.practicum.shareit.item.dto.ItemMapper.dtoToItem;
import static ru.practicum.shareit.item.dto.ItemMapper.itemToDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    @Override
    public ItemDto create(ItemDto item, long userId) {
        User creatingUser = getUser(userId, "create item: " + item);
        ItemRequest request = getItemRequest(item);
        Item itemCreated = itemRepository.save(
                dtoToItem(item, creatingUser, request));
        log.info("Item created: {}", itemCreated);
        return itemToDto(itemCreated, null, null);
    }

    @Override
    public ItemDto update(Long itemId, long userId, ItemDto itemDto) {
        Item targetItem = getItem(itemId, "update it");
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
        BookingsByItem dateByItem = getBookingsByItem(itemId);
        List<Comment> comments = commentRepository.findAllByItemIdIn(
                Collections.singletonList(itemId));
        return itemToDto(targetItem, dateByItem, comments);
    }

    @Override
    public ItemDto getById(long itemId, long userId) {
        Item targetItem = getItem(itemId, "get it");
        log.info("Item found: {}", targetItem);
        BookingsByItem dateByItem = targetItem.getOwner().getId()
                .equals(userId) ?
                getBookingsByItem(itemId) :
                null;
        List<Comment> comments = commentRepository.findAllByItemIdIn(
                Collections.singletonList(itemId));
        return itemToDto(targetItem, dateByItem, comments);
    }

    @Override
    public List<ItemDto> getAllForUser(long userId) {
        User user = getUser(userId, "get own items");
        Map<Long, Item> itemMap = itemListToMap(
                itemRepository.findAllByOwnerId(
                        user.getId()));
        log.info("Found {} items of user with id: {}", itemMap.size(), userId);
        Map<Long, BookingsByItem> bookingMap = getBookingsMap(itemMap);
        Map<Long, List<Comment>> commentMap = getCommentMap(itemMap);
        return getItemDtos(itemMap, bookingMap, commentMap);
    }

    @Override
    public List<ItemDto> searchByNameAndDescr(String text, long userId, Pageable page) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    String.format("User with id %s not found when trying to search for item by %s", userId, text));
        }
        if (text.isBlank()) {
            log.info("No text for search entered");
            return Collections.emptyList();
        }
        Map<Long, Item> foundItems = itemListToMap(itemRepository
                .findAllByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                        text, text, page));
        log.info("Found {} items containing {}", foundItems.size(), text);
        Map<Long, List<Comment>> commentMap = getCommentMap(foundItems);
        return getItemDtos(foundItems, Collections.emptyMap(), commentMap);
    }

    @Override
    public CommentDto create(CommentDto comment, long itemId, long userId) {
        User author = getUser(userId, "create comment: " + comment);
        Item item = getItem(itemId, "comment it");
        isItemRentedByUser(itemId, userId);
        Comment commentCreated = commentRepository.save(
                dtoToComment(comment, item, author));
        log.info("Comment created: {}", commentCreated);
        return commentToDto(commentCreated);
    }

    private User getUser(long id, String operation) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with id %s not found when trying to %s", id, operation)));
    }

    private Item getItem(long id, String operation) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Item with id %s not found when trying to %s", id, operation)));
    }

    private ItemRequest getItemRequest(ItemDto item) {
        return item.getRequestId() != null ?
                requestRepository.findById(item.getRequestId())
                        .orElse(null) :
                null;
    }

    private BookingsByItem getBookingsByItem(long itemId) {
        return bookingRepository.findDatesByItemId(
                        Collections.singletonList(itemId),
                        now())
                .stream()
                .findAny()
                .orElse(null);
    }

    private Map<Long, Item> itemListToMap(List<Item> itemList) {
        return itemList.stream()
                .collect(Collectors.toMap(
                        Item::getId, Function.identity()));
    }

    private Map<Long, BookingsByItem> getBookingsMap(Map<Long, Item> itemMap) {
        return bookingRepository.findDatesByItemId(
                        new ArrayList<>(itemMap.keySet()),
                        now())
                .stream()
                .collect(Collectors.toMap(
                        BookingsByItem::getItemId, Function.identity()));
    }

    private Map<Long, List<Comment>> getCommentMap(Map<Long, Item> itemMap) {
        return commentRepository.findAllByItemIdIn(
                        new ArrayList<>(itemMap.keySet()))
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId()));
    }

    private List<ItemDto> getItemDtos(Map<Long, Item> itemMap,
                                      Map<Long, BookingsByItem> bookingMap,
                                      Map<Long, List<Comment>> commentMap) {
        return itemMap.values().stream()
                .map(item -> itemToDto(
                        item,
                        bookingMap.get(item.getId()),
                        commentMap.get(item.getId()))
                )
                .collect(Collectors.toList());
    }

    private void isItemRentedByUser(long itemId, long userId) {
        if (!bookingRepository.existsByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId, userId, now(), APPROVED)) {
            throw new BadRequestException(
                    String.format("User with id: %s did not rent item with id: %s to comment it", userId, itemId));
        }
    }
}
