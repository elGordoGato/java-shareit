package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ItemMapper {
    public static Item dtoToItem(ItemDto dto, User user) {
        return new Item(dto.getId(), dto.getName(), dto.getDescription(), dto.getAvailable(), user);
    }

    public static ItemDto itemToDto(Item item, BookingsByItem bookingsByItem1, List<Comment> comments) {
        Optional<BookingsByItem> datesByItem = Optional.ofNullable(bookingsByItem1);
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
        if (datesByItem.isPresent()) {
            if (datesByItem.get().getLastBooking() != null) {
                itemDto.setLastBooking(BookingMapper.bookingToShort(datesByItem.get().getLastBooking()));
            }
            if (datesByItem.get().getNextBooking() != null) {
                itemDto.setNextBooking(BookingMapper.bookingToShort(datesByItem.get().getNextBooking()));
            }
            itemDto.setRentCounter(datesByItem.get().getRentCounter());
        }
        if (comments == null) {
            itemDto.setComments(Collections.emptyList());
        } else {
            itemDto.setComments(CommentMapper.commentToDto(comments));
        }
        log.info(itemDto.toString());
        return itemDto;
    }
}
