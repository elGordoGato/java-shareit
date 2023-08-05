package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, List<String>> handleConflictException(final ConflictException e) {
        List<String> errors = List.of(e.getMessage());
        log.warn(errors.toString());
        return Map.of(HttpStatus.CONFLICT.getReasonPhrase(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> handleNotValidException(final MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        log.warn(errors.toString());
        return Map.of(HttpStatus.BAD_REQUEST.getReasonPhrase(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, List<String>> handleNotFoundException(final NotFoundException e) {
        List<String> errors = List.of(e.getMessage());
        log.warn(errors.toString());
        return Map.of(HttpStatus.NOT_FOUND.getReasonPhrase(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        List<String> errors = List.of(e.getLocalizedMessage());
        log.warn(errors.toString());
        return Map.of(HttpStatus.BAD_REQUEST.getReasonPhrase(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, List<String>> handleForbiddenException(final ForbiddenException e) {
        List<String> errors = List.of(e.getMessage());
        log.warn(errors.toString());
        return Map.of(HttpStatus.FORBIDDEN.getReasonPhrase(), errors);
    }
}