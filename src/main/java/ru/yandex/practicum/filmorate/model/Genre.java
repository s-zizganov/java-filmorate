package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;

// Перечисление Genre представляет возможные жанры фильма
public enum Genre {
    COMEDY("Комедия"),
    DRAMA("Драма"),
    ANIMATION("Мультфильм"),
    THRILLER("Триллер"),
    DOCUMENTARY("Документальный"),
    ACTION("Боевик");

    // Поле для хранения названия жанра на русском языке
    private final String name;

    // Конструктор перечисления, принимает название жанра
    Genre(String name) {
        this.name = name;
    }

    // Геттер для получения названия жанра, используется для сериализации в JSON
    @JsonValue
    public String getName() {
        return name;
    }

    // Переопределяем метод toString, чтобы возвращать название жанра
    @Override
    public String toString() {
        return name;
    }
}