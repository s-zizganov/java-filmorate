package ru.yandex.practicum.filmorate.exception;

// Исключение для случаев, когда объект не найден (соответствует стилю Catsgram).
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}