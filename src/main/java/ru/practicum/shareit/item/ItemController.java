package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;


    @PostMapping
    public ItemDto create(@RequestBody @Valid ItemDto item, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user with id: {} to create item: {}", userId, item);
        return itemService.create(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable Long itemId,
                          @RequestHeader("X-Sharer-User-Id") long userId,
                          @RequestBody ItemDto itemDto) {
        log.info("Received request from user with Id {} to update item with id: {}, new data: {}",
                userId, itemId, itemDto);
        return itemService.update(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to get item with id: {}", userId, itemId);
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllForUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to get all items", userId);
        return itemService.getAllForUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByNameAndDescr(@RequestParam String text, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to search items containing: {}", userId, text);
        return itemService.searchByNameAndDescr(text, userId);
    }

    //POST /items/{itemId}/comment
    @PostMapping("/{itemId}/comment")
    public CommentDto create(@RequestBody @Valid CommentDto comment, @PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) throws NoSuchMethodException, MethodArgumentNotValidException {
        log.info("Received request from user with id: {} to create comment: {} about item with id: {} ", userId, comment, itemId);
        return itemService.create(comment, itemId, userId);
    }
}
