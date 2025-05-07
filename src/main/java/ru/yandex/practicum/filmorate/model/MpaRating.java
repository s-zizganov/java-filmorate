package ru.yandex.practicum.filmorate.model;


import com.fasterxml.jackson.annotation.JsonValue;

// Перечисление MpaRating представляет возможные рейтинги MPA для фильмов
public enum MpaRating {
    G("G"),         // Нет возрастных ограничений
    PG("PG"),       // Детям рекомендуется смотреть с родителями
    PG_13("PG-13"), // Детям до 13 лет просмотр нежелателен
    R("R"),         // Лицам до 17 лет просмотр только со взрослым
    NC_17("NC-17"); // Лицам до 18 лет просмотр запрещён

    // Поле для хранения строкового представления рейтинга
    private final String rating;

    // Конструктор перечисления, принимает строковое представление рейтинга
    MpaRating(String rating) {
        this.rating = rating;
    }

    // Геттер для получения строкового представления рейтинга, используется для сериализации в JSON
    @JsonValue
    public String getRating() {
        return rating;
    }

    // Переопределяем метод toString, чтобы возвращать строковое представление рейтинга
    @Override
    public String toString() {
        return rating;
    }
}