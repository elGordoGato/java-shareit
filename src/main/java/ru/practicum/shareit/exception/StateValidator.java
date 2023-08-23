package ru.practicum.shareit.exception;

import ru.practicum.shareit.booking.state.State;

public class StateValidator {

    public static State checkState(String state) {
        try {
            return State.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(state);
        }
    }
}
