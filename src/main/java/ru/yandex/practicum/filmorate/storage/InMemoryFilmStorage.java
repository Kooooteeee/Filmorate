package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        log.info("Получен запрос GET /films");
        return films.values();
    }

    @Override
    public Film create(Film film) {
        log.info("Получен запрос POST /films");
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации name: {}", film.getName());
            throw new ValidationException("Пустое название.");
        }
        if (film.getDescription().length() > 200) {
            log.error("Ошибка валидации description: {}", film.getDescription());
            throw new ValidationException("Описание должно быть короче 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            log.error("Ошибка валидации releaseDate: {}", film.getReleaseDate());
            throw new ValidationException("Некорректная дата.");
        }
        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации duration: {}", film.getDuration());
            throw new ValidationException("некорректная продолжительность.");
        }

        film.setId(getNextId());

        films.put(film.getId(), film);
        log.info("Фильм создан: {}", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Получен запрос PUT /films");
        Film oldFilm = films.get(newFilm.getId());

        if (oldFilm == null) {
            log.error("Попытка обновить несуществующий фильм id={}", newFilm.getId());
            throw new NotFoundException("Фильм не найден.");
        }

        if (newFilm.getDescription() != null) oldFilm.setDescription(newFilm.getDescription());
        if (newFilm.getName() != null) oldFilm.setName(newFilm.getName());
        if (newFilm.getReleaseDate() != null) oldFilm.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getDuration() != null) oldFilm.setDuration(newFilm.getDuration());

        log.info("Фильм обновлен: {}", oldFilm);
        return oldFilm;
    }

    @Override
    public Film findById(long id) {
        if (!films.containsKey(id)) {
            log.error("Попытка найти несуществующий фильм id={}", id);
            throw new NotFoundException("Фильм не найден.");
        }
        return  films.get(id);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
