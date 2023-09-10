package ru.practicum.shareit.booking;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Entity
@Table(name = "bookings")
@NoArgsConstructor
@Getter
@Setter
@ToString
@SqlResultSetMapping(name = "bookingsByItemMapping", classes = {
        @ConstructorResult(targetClass = BookingsByItem.class,
                columns = {@ColumnResult(name = "itemId"), @ColumnResult(name = "last"), @ColumnResult(name = "next"), @ColumnResult(name = "rent")})
})
@NamedNativeQuery(name="findDatesByItemId", query="SELECT bi.itemId itemId, b1 last, b2 next, bi.rent rent)\n " +
        "FROM (SELECT b.item_id itemId, MAX(b1.id) AS last, MIN(b2.id) AS next, COUNT(b.id) AS rent\n " +
        "      FROM bookings b\n " +
        "               LEFT JOIN bookings b1 ON b.id = b1.id and b1.start_date = (Select max(b.start_date) from bookings b where b.start_date <= ?2 AND b.item_id = b1.item_id)\n " +
        "               LEFT JOIN bookings b2 ON b.id = b2.id and b2.start_date = (Select min(b.start_date) from bookings b where b.start_date > ?2 AND b.item_id = b2.item_id)\n " +
        "      WHERE b.item_id IN (?1)\n " +
        "      GROUP BY b.item_id) bi\n " +
        "LEFT JOIN bookings b1 ON b1.item_id = bi.itemId and b1.id = bi.last\n " +
        "LEFT JOIN bookings b2 ON b2.item_id = bi.itemId and b2.id = bi.next ", resultSetMapping="bookingsByItemMapping")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    @Enumerated(EnumType.STRING)
    private Status status = Status.WAITING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    private User booker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    private Item item;

}
