package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Аннотация @WebMvcTest указывает, что мы тестируем контроллер UserController
// Spring загрузит только компоненты, связанные с MVC (контроллеры, обработчики исключений и т.д.), а остальные зависимости замокит
@WebMvcTest(UserController.class)
class UserControllerTest {

    // Объявляем переменную mockMvc для выполнения HTTP-запросов в тестах
    // @Autowired автоматически внедряет MockMvc, созданный Spring для тестирования
    @Autowired
    private MockMvc mockMvc;

    // Объявляем переменную objectMapper для преобразования объектов в JSON и обратно
    // @Autowired автоматически внедряет ObjectMapper, созданный Spring
    @Autowired
    private ObjectMapper objectMapper;

    // Объявляем мок-объект userStorage для имитации хранилища пользователей
    // @MockBean создаёт мок и добавляет его в контекст Spring, заменяя реальное хранилище
    // Исправлено: добавлен квалификатор name = "userDbStorage" для соответствия ожиданиям UserController
    @MockBean(name = "userDbStorage")
    private UserStorage userStorage;

    // Объявляем мок-объект userService для имитации сервиса пользователей
    // @MockBean создаёт мок и добавляет его в контекст Spring, заменяя реальный сервис
    @MockBean
    private UserService userService;

    // Объявляем переменную user для хранения тестового объекта User - пользователя
    private User user;
    // Объявляем переменную friend для хранения тестового объекта User - друга пользователя
    private User friend;

    // Выполняется перед каждым тестом
    // Подгтавливает тестовые данные, которые будут использоваться в тестах
    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@example.com");
        friend.setLogin("friendlogin");
        friend.setName("Friend Name");
        friend.setBirthday(LocalDate.of(1992, 2, 2));
    }

    @Test
    void shouldGetUserById() throws Exception {
        user.setId(1L);
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.login").value("userlogin"))
                .andExpect(jsonPath("$.name").value("User Name"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
    }

    @Test  // Проверяет, что пользователя можно успешно получить по ID через GET-запрос
    void shouldFailWhenUserNotFoundById() throws Exception {
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Пользователь с ID 999 не найден"));
    }

    @Test // Проверяет, что при запросе несуществующего пользователя возвращается ошибка 404
    void shouldCreateUserSuccessfully() throws Exception {
        when(userStorage.existsByEmail(anyString())).thenReturn(false);
        when(userStorage.create(any(User.class))).thenAnswer(invocation -> {
            User userToCreate = invocation.getArgument(0);
            userToCreate.setId(1L);
            return userToCreate;
        });

        // Выполняем POST-запрос на /users через MockMvc, чтобы создать нового пользователя
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                // Проверяем, что статус ответа — 200 (OK), то есть пользователь успешно создан
                .andExpect(status().isOk())
                // Проверяем, что в JSON-ответе поле id равно 1
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.login").value("userlogin"))
                .andExpect(jsonPath("$.name").value("User Name"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
    }

    @Test// Проверяет, что пользователь с пустым именем создаётся корректно (имя заменяется логином)
    void shouldCreateUserWithEmptyName() throws Exception {
        // Устанавливаем имя пользователя пустым (нарушение, но логика контроллера заменит его логином)
        user.setName("");
        // Настраиваем мок userStorage: при вызове existsByEmail с любым email возвращаем false (email не занят)
        when(userStorage.existsByEmail(anyString())).thenReturn(false);
        when(userStorage.create(any(User.class))).thenAnswer(invocation -> {
            // Получаем пользователя, которого передали в метод create
            User userToCreate = invocation.getArgument(0);
            // Устанавливаем ID пользователя равным 1
            userToCreate.setId(1L);
            return userToCreate;
        });

        // Выполняем POST-запрос на /users через MockMvc, чтобы создать пользователя с пустым именем
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.login").value("userlogin"))
                .andExpect(jsonPath("$.name").value("userlogin"))
                .andExpect(jsonPath("$.birthday").value("1990-01-01"));
    }

    @Test // Проверяет, что существующего пользователя можно успешно обновить через PUT-запрос
    void shouldUpdateUserSuccessfully() throws Exception {
        // Настраиваем мок userStorage: при вызове existsByEmail с любым email возвращаем false (email не занят)
        when(userStorage.existsByEmail(anyString())).thenReturn(false);
        // Настраиваем мок userStorage: при вызове create с любым пользователем возвращаем пользователя с ID = 1
        when(userStorage.create(any(User.class))).thenAnswer(invocation -> {
            // Получаем пользователя, которого передали в метод create
            User userToCreate = invocation.getArgument(0);
            // Устанавливаем ID пользователя равным 1
            userToCreate.setId(1L);
            // Возвращаем пользователя с установленным ID
            return userToCreate;
        });

        // Выполняем POST-запрос на /users через MockMvc, чтобы создать нового пользователя
        // Сохраняем ответ (JSON-строку) в переменную response
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Преобразуем JSON-ответ (строку) обратно в объект User, чтобы получить созданного пользователя
        User createdUser = objectMapper.readValue(response, User.class);

        // Создаём новый объект User для обновления
        User updatedUser = new User();
        updatedUser.setId(createdUser.getId());
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedlogin");
        updatedUser.setName("Updated Name");
        updatedUser.setBirthday(LocalDate.of(1995, 5, 5));

        // Настраиваем мок userStorage: при вызове findById с ID созданного пользователя возвращаем Optional
        // с созданным пользователем
        when(userStorage.findById(createdUser.getId())).thenReturn(Optional.of(createdUser));
        // Настраиваем мок userStorage: при вызове existsByEmail с новым email возвращаем false (email не занят)
        when(userStorage.existsByEmail("updated@example.com")).thenReturn(false);
        // Настраиваем мок userStorage: при вызове update с любым пользователем возвращаем
        // обновлённого пользователя (updatedUser)
        when(userStorage.update(any(User.class))).thenReturn(updatedUser);

        // Выполняем PUT-запрос на /users через MockMvc, чтобы обновить пользователя
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.login").value("updatedlogin"))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.birthday").value("1995-05-05"));
    }

    @Test // Проверяет, что можно получить список всех пользователей через GET-запрос
    void shouldGetAllUsers() throws Exception {
        when(userStorage.existsByEmail(anyString())).thenReturn(false);
        when(userStorage.create(any(User.class))).thenAnswer(invocation -> {
            User userToCreate = invocation.getArgument(0);
            userToCreate.setId(1L);
            return userToCreate;
        });

        // Выполняем POST-запрос на /users через MockMvc, чтобы создать нового пользователя
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Настраиваем мок userStorage: при вызове findAll возвращаем список, содержащий нашего тестового пользователя
        when(userStorage.findAll()).thenReturn(List.of(user));

        // Выполняем GET-запрос на /users через MockMvc, чтобы получить список всех пользователей
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                // Проверяем, что в JSON-ответе первый пользователь (индекс 0) имеет поле email равное "user@example.com"
                .andExpect(jsonPath("$[0].email").value("user@example.com"));
    }

    @Test // Проверяет, что при создании пользователя с пустым email возвращается ошибка
    void shouldFailWhenEmailIsBlank() throws Exception {
        user.setEmail("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Email пользователя не может быть пустым"));
    }

    @Test // Проверяет, что при создании пользователя с некорректным email (без @) возвращается ошибка
    void shouldFailWhenEmailIsInvalid() throws Exception {
        user.setEmail("invalid-email");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Email должен содержать символ @"));
    }

    @Test // Проверяет, что при создании пользователя с пустым логином возвращается ошибка
    void shouldFailWhenLoginIsBlank() throws Exception {
        user.setLogin("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Логин пользователя не может быть пустым"));
    }

    @Test // Проверяет, что при создании пользователя с логином, содержащим пробелы, возвращается ошибка
    void shouldFailWhenLoginHasSpaces() throws Exception {
        user.setLogin("user login");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Логин не может содержать пробелы"));
    }

    @Test // Проверяет, что при создании пользователя с датой рождения в будущем возвращается ошибка
    void shouldFailWhenBirthdayIsInFuture() throws Exception {
        user.setBirthday(LocalDate.now().plusDays(1));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Дата рождения не может быть в будущем"));
    }

    @Test // Проверяет, что при отправке пустого запроса на создание пользователя возвращается ошибка
    void shouldFailWhenRequestIsEmpty() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test // Проверяет, что при попытке обновить пользователя без указания ID возвращается ошибка
    void shouldFailWhenUpdatingWithNullId() throws Exception {
        user.setId(null);
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("ID пользователя должен быть указан"));
    }

    @Test // Проверяет, что при попытке обновить несуществующего пользователя возвращается ошибка 404
    void shouldFailWhenUpdatingNonExistentUser() throws Exception {
        user.setId(999L);
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Пользователь с ID 999 не найден"));
    }

    @Test // Проверяет, что друга можно успешно добавить через PUT-запрос
    void shouldAddFriend() throws Exception {
        user.setId(1L);
        friend.setId(2L);
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        // Проверяем, что дружба односторонняя
        when(userService.getFriends(1L)).thenReturn(List.of(friend));
        when(userService.getFriends(2L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));

        mockMvc.perform(get("/users/2/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test // Проверяет, что друга можно успешно удалить через DELETE-запрос
    void shouldRemoveFriend() throws Exception {
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(userStorage.findById(2L)).thenReturn(Optional.of(friend));
        when(userStorage.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test // Проверяет, что можно получить список друзей пользователя через GET запрос
    void shouldGetFriends() throws Exception {
        // Подготавливаем данные
        user.setId(1L);
        friend.setId(2L);

        // Настраиваем поведение userService.getFriends
        when(userService.getFriends(1L)).thenReturn(List.of(friend));

        // Выполняем запрос и проверяем результат
        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].email").value("friend@example.com"));
    }

    @Test // Проверяет, что можно получить список общих друзей двух пользователей через GET-запрос
    void shouldGetCommonFriends() throws Exception {
        // Создаём тестового пользователя - общего друга
        User commonFriend = new User();
        // Устанавливаем ID общего друга равным 3
        commonFriend.setId(3L);
        commonFriend.setEmail("common@example.com");
        commonFriend.setLogin("commonlogin");
        commonFriend.setName("Common Friend");
        commonFriend.setBirthday(LocalDate.of(1993, 3, 3));

        // Настраиваем мок userService: при вызове getCommonFriends с ID = 1 и ID = 2 возвращаем список,
        // содержащий общего друга
        when(userService.getCommonFriends(1L, 2L)).thenReturn(List.of(commonFriend));

        // Выполняем GET-запрос на /users/1/friends/common/2 через MockMvc, чтобы получить список общих
        // друзей пользователей с ID = 1 и ID = 2
        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                // Проверяем, что в JSON-ответе первый общий друг (индекс 0) имеет поле id равное 3
                .andExpect(jsonPath("$[0].id").value(3))
                // Проверяем, что в JSON-ответе первый общий друг (индекс 0) имеет поле email равное "common@example.com"
                .andExpect(jsonPath("$[0].email").value("common@example.com"));
    }
}