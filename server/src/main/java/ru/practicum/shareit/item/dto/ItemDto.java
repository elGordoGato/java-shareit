package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import ru.practicum.shareit.booking.dto.BookingShort;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@ToString
@Builder
public class ItemDto {
    private final Long id;

    private final String name;

    private final String description;

    private final Boolean available;


    private BookingShort lastBooking;


    private BookingShort nextBooking;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommentDto> comments;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long rentCounter;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long requestId;
}
