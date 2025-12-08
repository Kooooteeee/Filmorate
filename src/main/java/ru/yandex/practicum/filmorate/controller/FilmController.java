package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос GET /films");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен запрос POST /films");
            if (film.getName() == null || film.getName().isBlank()) {
                log.warn("Ошибка валидации name: {}", film.getName());
                throw new ValidationException("Пустое название.");
            }
            if (film.getDescription().length() > 200) {
                log.warn("Ошибка валидации description: {}", film.getDescription());
                throw new ValidationException("Описание должно быть короче 200 символов.");
            }
            if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
                log.warn("Ошибка валидации releaseDate: {}", film.getReleaseDate());
                throw new ValidationException("Некорректная дата.");
            }
            if (film.getDuration() <= 0) {
                log.warn("Ошибка валидации duration: {}", film.getDuration());
                throw new ValidationException("некорректная продолжительность.");
            }

            film.setId(getNextId());

            films.put(film.getId(), film);
            log.info("Фильм создан: {}", film);
            
            return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Получен запрос PUT /films");
        Film oldFilm = films.get(newFilm.getId());

        if (oldFilm == null) {
            log.warn("Попытка обновить несуществующий фильм id={}", newFilm.getId());
            throw new ValidationException("Фильм не найден.");
        }

        if (newFilm.getDescription() != null) oldFilm.setDescription(newFilm.getDescription());
        if (newFilm.getName() != null) oldFilm.setName(newFilm.getName());
        if (newFilm.getReleaseDate() != null) oldFilm.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getDuration() != null) oldFilm.setDuration(newFilm.getDuration());

        log.info("Фильм обновлен: {}", oldFilm);
        return oldFilm;
    }
}
