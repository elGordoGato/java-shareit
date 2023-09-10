package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.booking.status.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {
    @Query("SELECT b " +
            "FROM Booking b " +
            "WHERE (?1 <= b.end) AND (?2 >= b.start)" +
            "AND b.status = 'APPROVED'")
    List<Booking> findAllByDateInterfering(LocalDateTime start, LocalDateTime end);

    @Query("SELECT b " +
            "FROM Booking b " +
            "         WHERE b.start = (SELECT MAX(b1.start) " +
            "                          FROM Booking b1 " +
            "                          WHERE b1.item.id = b.item.id " +
            "                          AND b1.start <= ?2) " +
            "         AND b.item.id IN ?1 " +
            "         AND b.status = 'APPROVED' ")
    List<Booking> findLastBookings(List<Long> itemIds, LocalDateTime now);

    @Query("SELECT b " +
            "FROM Booking b " +
            "         WHERE b.start = (SELECT MIN(b1.start) " +
            "                          FROM Booking b1 " +
            "                          WHERE b1.item.id = b.item.id " +
            "                          AND b1.start > ?2) " +
            "         AND b.item.id IN ?1 " +
            "         AND b.status = 'APPROVED' ")
    List<Booking> findNextBookings(List<Long> itemIds, LocalDateTime now);


    boolean existsByItemIdAndBookerIdAndEndBeforeAndStatus(Long itemId, Long bookerId, LocalDateTime now, Status approved);

    Optional<Booking> findByIdAndBookerIdNot(long bookingId, long userId);

    @Query(" SELECT b " +
            "FROM Booking b " +
            "WHERE b.id = ?1 " +
            "AND (b.booker.id = ?2 OR b.item.owner.id = ?2) ")
    Optional<Booking> findByIdAndByBookerOrOwner(long bookingId, long userId);
}

/*("SELECT new ru.practicum.shareit.booking.dto.BookingsByItem( " +
        "   b1.item.id, b2, b3, COUNT(b1.id)) " +
        "FROM Booking b1 " +
        "LEFT JOIN Booking b2 ON b1.item.id = b2.item.id " +
        "AND b2.start = (SELECT MAX(b4.start) " +
        "                 FROM Booking b4 " +
        "                 WHERE b4.start <= ?2 AND b4.item.id IN ?1 " +
        "                 GROUP BY b4.item.id) " +
        "LEFT JOIN Booking b3 ON b1.item.id = b3.item.id " +
        "AND b3.start = (SELECT MIN(b5.start) " +
        "                 FROM Booking b5 " +
        "                 WHERE b5.start > ?2 AND b5.item.id IN ?1 " +
        "                 GROUP BY b5.item.id) " +
        "WHERE b1.item.id IN ?1 AND b1.status = 'APPROVED' " +
        "GROUP BY b1.item.id, b2, b3")*/

////////////////////////////////////////////////////////
/*"WITH bookingByItem AS ( " +
        "SELECT b.item.id AS itemId, " +
        "MAX(b1.id) AS last, " +
        "MIN(b2.id) AS next, " +
        "COUNT(b.id) AS rentCounter " +
        "FROM Booking b " +
        "LEFT JOIN Booking b1 ON b1.id = b.id " +
        "AND b1.start = (SELECT MAX(b.start) " +
        "                 FROM Booking b " +
        "                 WHERE b.start <= ?2 AND b.item.id = b1.item.id " +
        "                ) " +
        "LEFT JOIN Booking b2 ON b.id = b2.id " +
        "AND b2.start = (SELECT MIN(b.start) " +
        "                 FROM Booking b " +
        "                 WHERE b.start > ?2 AND b.item.id = b2.item.id " +
        "                ) " +
        "WHERE b1.item.id IN ?1 AND b1.status = 'APPROVED' " +
        "GROUP BY b.item.id) " +
        "SELECT new ru.practicum.shareit.booking.dto.BookingsByItem( " +
        "bi.itemId, b1, b2, bi.rentCounter " +
        "FROM bookingByItem bi " +
        "LEFT JOIN Booking b1 ON b1.item.it = bi.itemId AND b1.id = bi.last " +
        "LEFT JOIN Booking b2 ON b2.item.it = bi.itemId AND b2.id = bi.next ")*/


