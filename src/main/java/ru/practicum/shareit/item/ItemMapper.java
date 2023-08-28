package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ItemMapper {
    public static Item dtoToItem(ItemDto dto, User user) {
        return new Item(dto.getId(), dto.getName(), dto.getDescription(), dto.getAvailable(), user);
    }

    public static ItemDto itemToDto(Item item, BookingsByItem bookingsByItem, List<Comment> comments) {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(CommentMapper.commentToDto(
                        (comments != null) ? comments : Collections.emptyList()))
                .build();
        if (bookingsByItem != null) {
            Booking lastBooking = bookingsByItem.getLastBooking();
            Booking nextBooking = bookingsByItem.getNextBooking();
            itemDto.setLastBooking((lastBooking != null) ? BookingMapper.bookingToShort(lastBooking) : null);
            itemDto.setNextBooking((nextBooking != null) ? BookingMapper.bookingToShort(nextBooking) : null);
            itemDto.setRentCounter(bookingsByItem.getRentCounter());
        }
        return itemDto;
    }
}
