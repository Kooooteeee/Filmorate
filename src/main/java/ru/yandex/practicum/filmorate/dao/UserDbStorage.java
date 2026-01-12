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
import java.sql.Types;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final static String FIND_ALL_SQL = "SELECT id, email, login, name, birthday FROM users ORDER BY id";

    private final static String CREATE_SQL = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private final static String UPDATE_SQL = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    private final static String FIND_SQL = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";

    private final static String FIND_FRIENDS_SQL = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f
                JOIN users u ON u.id = f.friend_id
                WHERE f.user_id = ?
                ORDER BY u.id
                """;

    private final  static String MERGE_SQL = """
                MERGE INTO friendships (user_id, friend_id, status)
                KEY (user_id, friend_id)
                VALUES (?, ?, 'UNCONFIRMED')
                """;

    private final static String CONFIRM_BOTH_SQL = """
                    UPDATE friendships
                    SET status = 'CONFIRMED'
                    WHERE (user_id = ? AND friend_id = ?)
                       OR (user_id = ? AND friend_id = ?)
                    """;

    private final static String FIND_COMMON_FRIENDS_SQL = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN friendships f1 ON f1.friend_id = u.id
                JOIN friendships f2 ON f2.friend_id = u.id
                WHERE f1.user_id = ?
                  AND f2.user_id = ?
                ORDER BY u.id
                """;

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
        return jdbcTemplate.query(FIND_ALL_SQL, USER_MAPPER);
    }

    @Override
    public User create(User user) {
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
            PreparedStatement ps = con.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            if (user.getBirthday() == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(user.getBirthday()));
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) {

        int updated = jdbcTemplate.update(UPDATE_SQL,
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
        try {
            return jdbcTemplate.queryForObject(FIND_SQL, USER_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }

    @Override
    public void addFriend(long userId, long friendId) {
        jdbcTemplate.update(MERGE_SQL, userId, friendId);

        Integer reverseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                friendId, userId
        );

        if (reverseCount != null && reverseCount > 0) {
            jdbcTemplate.update(CONFIRM_BOTH_SQL, userId, friendId, friendId, userId);
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
        return jdbcTemplate.query(FIND_FRIENDS_SQL, USER_MAPPER, userId);
    }

    @Override
    public Collection<User> findCommonFriends(long userId, long otherId) {
        return jdbcTemplate.query(FIND_COMMON_FRIENDS_SQL, USER_MAPPER, userId, otherId);
    }
}
