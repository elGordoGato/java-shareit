package ru.practicum.shareit.booking.state;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum State {
    ALL("Все"),
    CURRENT("Текущие"),
    PAST("Прошедшие"),
    FUTURE("Будущие"),
    WAITING("Ожидают подтверждения"),
    REJECTED("Отклоненные");

    private final String operation;
}