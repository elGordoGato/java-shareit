package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;


    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @RequestBody @Valid ItemDto item) {
        log.info("Received request from user with id: {} to create item: {}", userId, item);
        return itemClient.create(userId, item);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Received request from user with Id {} to update item with id: {}, new data: {}",
                userId, itemId, itemDto);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable long itemId,
                                          @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to get item with id: {}", userId, itemId);
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllForUser(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Received request from user {} to get all items", userId);
        return itemClient.getAllForUser(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchByNameAndDescr(@RequestHeader("X-Sharer-User-Id") long userId,
                                                       @RequestParam String text,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                       @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Received request from user {} to search items containing: {}", userId, text);
        return itemClient.searchByNameAndDescr(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long itemId,
                                         @RequestBody @Valid CommentDto comment) {
        log.info("Received request from user with id: {} to create comment: {} about item with id: {} ",
                userId, comment, itemId);
        return itemClient.create(userId, itemId, comment);
    }
}
