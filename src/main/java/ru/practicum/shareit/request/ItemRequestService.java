package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestDto requestDto);

    List<ItemRequestDto> findByUserId(long userId);

    List<ItemRequestDto> findAll(long userId, Pageable pageRequest);

    ItemRequestDto findById(long userId, long requestId);
}
