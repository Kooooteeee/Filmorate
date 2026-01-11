/*package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Получен запрос GET /users");
        return users.values();
    }

    @Override
    public User create(User user) {
        log.info("Получен запрос POST /users");
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Ошибка валидации email: {}", user.getEmail());
            throw new ValidationException("Некорректная почта.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации login: {}", user.getLogin());
            throw new ValidationException("Некорректный логин.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.error("Имя не задано, подставлем login: {}", user.getName());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка валидации birthday: {}", user.getBirthday());
            throw new ValidationException("Некорректная дата рождения.");
        }

        user.setId(getNextId());

        users.put(user.getId(), user);
        log.info("Пользователь создан: {}", user);
        return user;
    }

    @Override
    public User update(User newUser) {
        log.info("Получен запрос PUT /users");
        User oldUser = users.get(newUser.getId());

        if (oldUser == null) {
            log.error("Попытка обновить несуществующего пользователя id={}", newUser.getId());
            throw new NotFoundException("Пользователь не найден.");
        }

        if (newUser.getLogin() != null) oldUser.setLogin(newUser.getLogin());
        if (newUser.getName() != null) oldUser.setName(newUser.getName());
        if (newUser.getBirthday() != null) oldUser.setBirthday(newUser.getBirthday());
        if (newUser.getEmail() != null) oldUser.setEmail(newUser.getEmail());
        log.info("Пользователь обновлен: {}", oldUser);
        return oldUser;
    }

    @Override
    public User findById(long id) {
        if (!users.containsKey(id)) {
            log.error("Попытка найти несуществующего пользователя id={}", id);
            throw new NotFoundException("Пользователь не найден.");
        }
        return  users.get(id);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}*/
