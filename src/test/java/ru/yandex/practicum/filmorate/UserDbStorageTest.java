package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void shouldCreateAndFindById() {
        User u = new User();
        u.setEmail("test@mail.com");
        u.setLogin("login");
        u.setName("name");
        u.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userStorage.create(u);

        assertThat(created.getId()).isNotNull();
        User found = userStorage.findById(created.getId());

        assertThat(found.getEmail()).isEqualTo("test@mail.com");
        assertThat(found.getLogin()).isEqualTo("login");
        assertThat(found.getName()).isEqualTo("name");
    }

    @Test
    void shouldUpdateUser() {
        User u = new User();
        u.setEmail("a@mail.com");
        u.setLogin("a");
        u.setName("A");
        u.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userStorage.create(u);

        User upd = new User();
        upd.setId(created.getId());
        upd.setEmail("b@mail.com");
        upd.setLogin("b");
        upd.setName("B");
        upd.setBirthday(LocalDate.of(1991, 2, 2));

        User updated = userStorage.update(upd);

        assertThat(updated.getEmail()).isEqualTo("b@mail.com");
        assertThat(updated.getLogin()).isEqualTo("b");
        assertThat(updated.getName()).isEqualTo("B");
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1991, 2, 2));
    }

    @Test
    void shouldThrowNotFoundOnMissingUser() {
        assertThatThrownBy(() -> userStorage.findById(99999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldAddAndRemoveFriend_oneSided() {
        User u1 = new User();
        u1.setEmail("u1@mail.com");
        u1.setLogin("u1");
        u1.setName("u1");
        u1.setBirthday(LocalDate.of(2000, 1, 1));
        u1 = userStorage.create(u1);

        User u2 = new User();
        u2.setEmail("u2@mail.com");
        u2.setLogin("u2");
        u2.setName("u2");
        u2.setBirthday(LocalDate.of(2000, 1, 2));
        u2 = userStorage.create(u2);

        userStorage.addFriend(u1.getId(), u2.getId());

        Collection<User> friendsOfU1 = userStorage.findFriends(u1.getId());
        assertThat(friendsOfU1).extracting(User::getId).contains(u2.getId());

        Collection<User> friendsOfU2 = userStorage.findFriends(u2.getId());
        assertThat(friendsOfU2).extracting(User::getId).doesNotContain(u1.getId());

        userStorage.removeFriend(u1.getId(), u2.getId());
        assertThat(userStorage.findFriends(u1.getId())).isEmpty();
    }

    @Test
    void shouldFindCommonFriends() {
        User a = createUser("a@mail.com", "a");
        User b = createUser("b@mail.com", "b");
        User c = createUser("c@mail.com", "c");
        User d = createUser("d@mail.com", "d");

        userStorage.addFriend(a.getId(), c.getId());
        userStorage.addFriend(a.getId(), d.getId());

        userStorage.addFriend(b.getId(), d.getId());

        Collection<User> common = userStorage.findCommonFriends(a.getId(), b.getId());
        assertThat(common).extracting(User::getId).containsExactly(d.getId());
    }

    private User createUser(String email, String login) {
        User u = new User();
        u.setEmail(email);
        u.setLogin(login);
        u.setName(login);
        u.setBirthday(LocalDate.of(2000, 1, 1));
        return userStorage.create(u);
    }
}
