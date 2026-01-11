package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        user.setBirthday(birthday != null ? birthday.toLocalDate() : null);
        return user;
    };

    @Override
    public Collection<User> findAll() {
        String sql = "SELECT id, email, login, name, birthday FROM users ORDER BY id";
        return jdbcTemplate.query(sql, USER_MAPPER);
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный login.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            if (user.getBirthday() == null) {
                ps.setNull(4, java.sql.Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(user.getBirthday()));
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int updated = jdbcTemplate.update(sql,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday() == null ? null : Date.valueOf(newUser.getBirthday()),
                newUser.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Пользователь не найден.");
        }

        return findById(newUser.getId());
    }

    @Override
    public User findById(long id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, USER_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String merge = """
                MERGE INTO friendships (user_id, friend_id, status)
                KEY (user_id, friend_id)
                VALUES (?, ?, 'UNCONFIRMED')
                """;
        jdbcTemplate.update(merge, userId, friendId);

        Integer reverseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                friendId, userId
        );

        if (reverseCount != null && reverseCount > 0) {
            String confirmBoth = """
                    UPDATE friendships
                    SET status = 'CONFIRMED'
                    WHERE (user_id = ? AND friend_id = ?)
                       OR (user_id = ? AND friend_id = ?)
                    """;
            jdbcTemplate.update(confirmBoth, userId, friendId, friendId, userId);
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);

        Integer reverseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                friendId, userId
        );

        if (reverseCount != null && reverseCount > 0) {
            jdbcTemplate.update(
                    "UPDATE friendships SET status = 'UNCONFIRMED' WHERE user_id = ? AND friend_id = ?",
                    friendId, userId
            );
        }
    }

    @Override
    public Collection<User> findFriends(long userId) {
        String sql = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f
                JOIN users u ON u.id = f.friend_id
                WHERE f.user_id = ?
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, USER_MAPPER, userId);
    }

    @Override
    public Collection<User> findCommonFriends(long userId, long otherId) {
        String sql = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN friendships f1 ON f1.friend_id = u.id
                JOIN friendships f2 ON f2.friend_id = u.id
                WHERE f1.user_id = ?
                  AND f2.user_id = ?
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, USER_MAPPER, userId, otherId);
    }
}
