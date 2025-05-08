package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Map;

/**
 * Класс GlobalExceptionHandler обрабатывает все исключения, которые возникают в контроллерах, перехватывает
 * ошибки и возвращает их клиенту в виде JSON с понятным сообщением.
 */
@RestControllerAdvice // Указывает, что этот класс обрабатывает исключения для всех контроллеров
public class GlobalExceptionHandler {

    // Обработчик исключений валидации (например, пустое имя или неверная дата)
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Устанавливает статус ответа 400
    public Map<String, String> handleValidationException(ValidationException ex) {
        return Map.of(
                "error", "Validation error",
                "message", ex.getMessage()
        );
    }

    // Обработчик исключений, когда объект не найден (например, фильма или пользователя с таким ID нет).
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Устанавливает статус ответа 404
    public Map<String, String> handleNotFoundException(NotFoundException ex) {
        return Map.of(
                "error", "Not found",
                "message", ex.getMessage()
        );
    }

    //Обработчик всех остальных необработанных исключений (общий случай).
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Устанавливает статус ответа 500
    public Map<String, String> handleGeneralException(Exception ex) {
        return Map.of(
                "error", "Internal server error",
                "message", ex.getMessage()
        );
    }

    // Обработчик исключений, когда тело запроса пустое или не может быть прочитано.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return Map.of(
                "error", "Validation error",
                "message", "Тело запроса не может быть пустым"
        );
    }

    // Метод handleDuplicatedDataException для обработки исключений, связанных с дублированием данных
    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Статус ответа 400
    public Map<String, String> handleDuplicatedDataException(DuplicatedDataException ex) {
        return Map.of("error", "Duplicated data", "message", ex.getMessage());
    }

    @ExceptionHandler(NotFoundDataException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundDataException(NotFoundDataException ex) {
        return Map.of(
                "error", "Not found",
                "message", ex.getMessage()
        );
    }
}