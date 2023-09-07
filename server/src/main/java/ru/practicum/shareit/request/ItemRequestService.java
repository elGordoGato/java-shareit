package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestDto requestDto);

    List<ItemRequestDto> getAllByUserId(long userId);

    List<ItemRequestDto> getAll(long userId, Pageable pageRequest);

    ItemRequestDto getById(long userId, long requestId);
}
