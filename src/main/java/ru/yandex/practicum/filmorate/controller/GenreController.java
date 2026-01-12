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

    private final static String FIND_ALL_SQL = "SELECT id, name FROM genres ORDER BY id";

    private final static String FIND_BY_SQL = "SELECT id, name FROM genres WHERE id = ?";

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
        return jdbcTemplate.query(FIND_ALL_SQL, GENRE_MAPPER);
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable int id) {
        log.info("GET /genres/{}", id);

        List<Genre> found = jdbcTemplate.query(FIND_BY_SQL, GENRE_MAPPER, id);
        if (found.isEmpty()) {
            throw new NotFoundException("Жанр не найден.");
        }
        return found.get(0);
    }
}
