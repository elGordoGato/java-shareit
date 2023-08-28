package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.booking.dto.BookingsByItem;
import ru.practicum.shareit.booking.status.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE (?1 <= b.end) AND (?2 >= b.start)" +
            "AND b.status = 'APPROVED'")
    List<Booking> findAllByDateInterfering(LocalDateTime start, LocalDateTime end);


    @Query("SELECT new ru.practicum.shareit.booking.dto.BookingsByItem( " +
            "   b1.item.id, b2, b3, COUNT(b1.id)) " +
            "FROM Booking b1 " +
            "LEFT JOIN Booking b2 ON b1.item.id = b2.item.id " +
            "AND b2.start IN (SELECT MAX(b4.start) " +
            "                 FROM Booking b4 " +
            "                 WHERE b4.start <= ?2 AND b4.item.id IN ?1 " +
            "                 GROUP BY b4.item.id) " +
            "LEFT JOIN Booking b3 ON b1.item.id = b3.item.id " +
            "AND b3.start IN (SELECT MIN(b5.start) " +
            "                 FROM Booking b5 " +
            "                 WHERE b5.start > ?2 AND b5.item.id IN ?1 " +
            "                 GROUP BY b5.item.id) " +
            "WHERE b1.item.id IN ?1 AND b1.status = 'APPROVED' " +
            "GROUP BY b1.item.id")
    List<BookingsByItem> findDatesByItemId(List<Long> itemIds, LocalDateTime now);


    boolean existsByItemIdAndBookerIdAndEndBeforeAndStatus(Long itemId, Long bookerId, LocalDateTime now, Status approved);
}
