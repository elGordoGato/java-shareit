package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.User;

import java.util.Objects;

/**
 * TODO Sprint add-controllers.
 */

@Data
@Builder
public class Item {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private User owner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
