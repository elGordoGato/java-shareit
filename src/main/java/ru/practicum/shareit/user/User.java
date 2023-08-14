package ru.practicum.shareit.user;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * TODO Sprint add-controllers.
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    @Email(message = "Email должен быть корректным адресом электронной почты")
    private String email;

    @Column(name = "registration_date")
    private Instant registrationDate = Instant.now();

}
