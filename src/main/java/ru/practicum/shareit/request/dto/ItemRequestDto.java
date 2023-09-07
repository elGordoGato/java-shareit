package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
public class ItemRequestDto {
    private Long id;
    @NotBlank
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd@HH:mm:ss.SSSD", locale = "ru_RU")
    private LocalDateTime created;

    private List<ItemDto> items;


}
