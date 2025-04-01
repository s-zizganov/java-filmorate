package ru.yandex.practicum.filmorate.exception;

// Исключение для случаев дублирования данных, например, email (соответствует стилю Catsgram).
public class DuplicatedDataException extends RuntimeException {
    public DuplicatedDataException(String message) {
        super(message);
    }
}