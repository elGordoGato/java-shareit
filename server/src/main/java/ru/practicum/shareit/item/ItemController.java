package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;


    @PostMapping
    public ItemDto create(@RequestBody ItemDto item,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
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
    public ItemDto getById(@PathVariable long itemId,
                           @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to get item with id: {}", userId, itemId);
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllForUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        Sort sort = Sort.by(DESC, "created");
        Pageable page = PageRequest.of(from / size, size, sort);
        log.info("Received request from user {} to get all items", userId);
        return itemService.getAllForUser(userId, page);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByNameAndDescr(@RequestParam String text,
                                              @RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        Sort sort = Sort.by(DESC, "created");
        Pageable page = PageRequest.of(from / size, size, sort);
        log.info("Received request from user {} to search items containing: {}", userId, text);
        return itemService.searchByNameAndDescr(text, userId, page);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto create(@RequestBody CommentDto comment,
                             @PathVariable long itemId,
                             @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user with id: {} to create comment: {} about item with id: {} ",
                userId, comment, itemId);
        return itemService.create(comment, itemId, userId);
    }
}
