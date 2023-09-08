package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> handleNotValidException(final MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        if (errors.isEmpty()) {
            errors.add((Objects.requireNonNull(e.getGlobalError())).getDefaultMessage());
        }
        log.warn(errors.toString());
        return Map.of(HttpStatus.BAD_REQUEST.getReasonPhrase(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        List<String> errors = List.of(e.getLocalizedMessage());
        log.warn(errors.toString());
        return Map.of(HttpStatus.BAD_REQUEST.getReasonPhrase(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, List<String>> handleConstraintViolationException(final ConstraintViolationException e) {
        List<String> errors = List.of(e.getLocalizedMessage());
        return Map.of(HttpStatus.BAD_REQUEST.getReasonPhrase(), errors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(final IllegalArgumentException e) {
        String error = e.getLocalizedMessage();
        log.warn(List.of(error).toString());
        return Map.of("error", error, HttpStatus.BAD_REQUEST.getReasonPhrase(), HttpStatus.BAD_REQUEST.toString());
    }


}