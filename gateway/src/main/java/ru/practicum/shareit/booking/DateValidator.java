package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<DateOrder, BookItemRequestDto> {

    @Override
    public boolean isValid(BookItemRequestDto request, ConstraintValidatorContext context) {
        if (request.getStart() == null || request.getEnd() == null)
            return false;
        else
            return request.getStart().isBefore(request.getEnd());
    }


}
