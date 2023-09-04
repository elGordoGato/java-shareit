package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatorTest {

    private final Validator validator;

    public ValidatorTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    public void testUser_ValidData() {
        // Arrange
        User myUser = new User();
        myUser.setName("John Doe");
        myUser.setEmail("john@doe.com");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(myUser);

        // Assert
        assertEquals(0, violations.size());
    }

    @Test
    public void testUser_InvalidData() {
        // Arrange
        User myUser = new User();
        myUser.setName("");
        myUser.setEmail("john-doe.com");

        // Act
        Set<ConstraintViolation<User>> violations = validator.validate(myUser);

        // Assert
        assertThat(violations, hasSize(2));
        assertThat(violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList()),
                containsInAnyOrder("не должно быть пустым",
                        "Email должен быть корректным адресом электронной почты"));
    }
}