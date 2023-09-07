package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @RequestBody @Valid ItemRequestDto requestDto) {
        log.info("Received request to create request for Item: {} from User: {}", requestDto, userId);
        return requestService.create(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Received request to get all own requests from user: {}",
                userId);
        return requestService.getAllByUserId(userId);
    }


    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                       @RequestParam(defaultValue = "10") @Positive int size) {
        Pageable page = PageRequest.of(from / size, size);
        log.info("Received request to get page with all requests from user: {} from {} element and size {}",
                userId,
                from,
                size);
        return requestService.getAll(userId, page);
    }


    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long requestId) {
        log.info("Received request to get request: {} from user: {}",
                requestId, userId);
        return requestService.getById(userId, requestId);
    }

}
