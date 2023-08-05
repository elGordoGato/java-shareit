package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class ItemDto {
    private final Long id;

    @NotBlank(message = "Необходимо указать название")
    private final String name;

    @NotBlank(message = "Необходимо указать описание")
    private final String description;

    @NotNull(message = "Необходимо указать доступность для аренды")
    private Boolean available;

    private Integer rentCounter;

}
