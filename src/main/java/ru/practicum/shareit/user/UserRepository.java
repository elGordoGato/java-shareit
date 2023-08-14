package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


  //  User update(User targetUser, User user);

    List<User> findByEmailContainingIgnoreCase(String emailSearch);

    //List<UserShort> findAllByEmailContainingIgnoreCase(String emailSearch);
}