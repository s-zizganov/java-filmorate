package ru.yandex.practicum.filmorate.exception;


// исключение, которое выбрасывается при ошибках валидации данных
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}