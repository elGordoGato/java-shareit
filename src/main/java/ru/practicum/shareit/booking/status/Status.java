package ru.practicum.shareit.booking.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    WAITING("Рассматривается"),
    APPROVED("Подтверждено"),
    REJECTED("Отклонено");

    private final String operation;
}
