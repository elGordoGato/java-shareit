package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    public ItemDto addNewItem(@RequestBody @Valid ItemDto item, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user with id: {} to create item: {}", userId, item);
        return itemService.addItem(item, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateExistedUser(@PathVariable Long itemId,
                                     @RequestHeader("X-Sharer-User-Id") long userId,
                                     @RequestBody ItemDto itemDto) {
        log.info("Received request to update item with id: {}, new data: {}", itemId, itemDto);
        return itemService.updateItem(itemId, userId, itemDto);
    }

    //GET /items/{itemId}
    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to get item with id: {}", userId, itemId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllUsers(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to get all items", userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemByNameAndDescr(@RequestParam String text, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request from user {} to search items containing: {}", userId, text);
        return itemService.searchItemByNameAndDescr(text, userId);
    }
}
