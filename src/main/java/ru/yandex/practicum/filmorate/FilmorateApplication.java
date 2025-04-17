package ru.yandex.practicum.filmorate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Аннотация @SpringBootApplication указывает, что это главный класс Spring Boot приложения
@SpringBootApplication
public class FilmorateApplication {
    public static void main(String[] args) {
        // Запускаем Spring Boot приложение, передавая класс FilmorateApplication и аргументы командной строки
        SpringApplication.run(FilmorateApplication.class, args);
    }

}
