package ru.practicum.shareit.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import ru.practicum.shareit.booking.dto.BookingShort;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@ToString
@Builder
public class ItemDto {
    private final Long id;

    @NotBlank(message = "Необходимо указать название")
    private final String name;

    @NotBlank(message = "Необходимо указать описание")
    private final String description;

    @NotNull(message = "Необходимо указать доступность для аренды")
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
