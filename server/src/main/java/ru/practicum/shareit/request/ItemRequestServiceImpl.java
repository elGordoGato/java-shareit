package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.dto.ItemRequestMapper.dtoToItemRequest;
import static ru.practicum.shareit.request.dto.ItemRequestMapper.itemRequestToDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRepository itemRepository;
    private final ItemRequestRepository requestRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestDto requestDto) {
        User requestingUser = userService.findById(userId);
        ItemRequest itemRequest = requestRepository.save(
                dtoToItemRequest(requestDto, requestingUser));
        log.info("Request created: {}", itemRequest);
        return itemRequestToDto(itemRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getAllByUserId(long userId) {
        User requestingUser = userService.findById(userId);
        Map<Long, ItemRequest> itemRequests = requestsToMap(
                requestRepository.findAllByAuthorId(
                        requestingUser.getId()));
        log.info("Found own requests: {}", itemRequests.values());
        Map<Long, List<Item>> responseItemsForRequests = getResponseItemsForRequests(itemRequests);
        return getItemRequestDtos(itemRequests, responseItemsForRequests);
    }


    @Override
    public List<ItemRequestDto> getAll(long userId, Pageable pageRequest) {
        User requestingUser = userService.findById(userId);
        Map<Long, ItemRequest> itemRequests = requestsToMap(
                requestRepository.findAllByAuthorIdNot(
                        requestingUser.getId(),
                        pageRequest));
        log.info("Found others requests: {}", itemRequests.values());
        Map<Long, List<Item>> responseItemsForRequests = getResponseItemsForRequests(itemRequests);
        return getItemRequestDtos(itemRequests, responseItemsForRequests);
    }


    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        userService.existsById(userId);
        ItemRequest itemRequest = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException(String.format("Item request with id: %s was not found", requestId)));
        log.info("Found request: {}", itemRequest);
        List<Item> items = itemRepository.findAllByRequestIdIn(Set.of(itemRequest.getId()));
        return itemRequestToDto(itemRequest, items);
    }


    private Map<Long, ItemRequest> requestsToMap(List<ItemRequest> requestList) {
        return requestList.stream()
                .collect(Collectors.toMap(
                        ItemRequest::getId, Function.identity()));
    }

    private Map<Long, List<Item>> getResponseItemsForRequests(Map<Long, ItemRequest> itemRequests) {
        return itemRepository.findAllByRequestIdIn(itemRequests.keySet())
                .stream()
                .collect(
                        Collectors.groupingBy(item -> item.getRequest().getId()));
    }

    private List<ItemRequestDto> getItemRequestDtos(Map<Long, ItemRequest> itemRequests,
                                                    Map<Long, List<Item>> responseItemsForRequests) {
        return itemRequests.values()
                .stream()
                .map(itemRequest ->
                        itemRequestToDto(
                                itemRequest,
                                responseItemsForRequests.get(itemRequest.getId())))
                .collect(Collectors.toList());
    }
}
