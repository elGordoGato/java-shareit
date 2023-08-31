package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByAuthorId(Long id);

    List<ItemRequest> findAllByIdNotIn(List<Long> id, Pageable pageRequest);

    List<ItemRequest> findByIdNotIn(List<Long> requestsOfUserItems, Pageable pageRequest);

    List<ItemRequest> findAllByAuthorIdNot(Long id, Pageable pageRequest);
}