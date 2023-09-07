package ru.practicum.shareit.fromServer;

import ru.practicum.shareit.booking.dto.BookingRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<DateOrder, BookingRequest> {

    @Override
    public boolean isValid(BookingRequest request, ConstraintValidatorContext context) {
        if (request.getStart() == null || request.getEnd() == null)
            return false;
        else
            return request.getStart().isBefore(request.getEnd());
    }


}
