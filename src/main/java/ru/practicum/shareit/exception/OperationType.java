package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationType {
    CREATE("создания"),
    GET("получения"),
    UPDATE("изменения"),
    DELETE("удаления");

    private final String operation;
}
