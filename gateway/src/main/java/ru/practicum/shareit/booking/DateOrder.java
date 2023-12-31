package ru.practicum.shareit.booking;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DateValidator.class)
public @interface DateOrder {
    String message() default "End date should be after start date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
