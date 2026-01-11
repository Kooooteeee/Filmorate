package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Genre> GENRE_MAPPER = (rs, rowNum) -> {
        Genre g = new Genre();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        return g;
    };

    @GetMapping
    public List<Genre> findAll() {
        log.info("GET /genres");
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, GENRE_MAPPER);
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        log.info("GET /genres/{}", id);
        String sql = "SELECT id, name FROM genres WHERE id = ?";

        List<Genre> found = jdbcTemplate.query(sql, GENRE_MAPPER, id);
        if (found.isEmpty()) {
            throw new NotFoundException("Жанр не найден.");
        }
        return found.get(0);
    }
}
