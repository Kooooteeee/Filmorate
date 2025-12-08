package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

	@Autowired
	private UserController userController;

	@Autowired
	private FilmController filmController;

	@Test
	void shouldCreateValidUser() {
		User user = new User();
		user.setEmail("test@mail.com");
		user.setLogin("login");
		user.setName("name");
		user.setBirthday(LocalDate.now().minusYears(20));

		User created = userController.create(user);

		assertNotNull(created);
		assertEquals(1L, created.getId());
		assertEquals("name", created.getName());
		assertEquals("login", created.getLogin());
		assertEquals("test@mail.com", created.getEmail());
	}

	@Test
	void shouldSetNameToLoginWhenNameIsBlank() {
		User user = new User();
		user.setEmail("test@mail.com");
		user.setLogin("login");
		user.setName(" ");
		user.setBirthday(LocalDate.now().minusYears(20));

		User created = userController.create(user);

		assertEquals("login", created.getName());
	}

	@Test
	void shouldThrowWhenEmailIsInvalid() {
		User user = new User();
		user.setEmail("wrong-mail");
		user.setLogin("login");
		user.setName("name");
		user.setBirthday(LocalDate.now().minusYears(20));

		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	void shouldThrowWhenLoginContainsSpace() {
		User user = new User();
		user.setEmail("test@mail.com");
		user.setLogin("bad login");
		user.setName("name");
		user.setBirthday(LocalDate.now().minusYears(20));

		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	void shouldThrowWhenBirthdayInFuture() {
		User user = new User();
		user.setEmail("test@mail.com");
		user.setLogin("login");
		user.setName("name");
		user.setBirthday(LocalDate.now().plusDays(1));

		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	private Film createValidFilm() {
		Film film = new Film();
		film.setName("Film");
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(100);
		return film;
	}

	@Test
	void shouldCreateValidFilm() {
		Film film = createValidFilm();

		Film created = filmController.create(film);

		assertNotNull(created);
		assertEquals(1L, created.getId());
		assertEquals("Film", created.getName());
	}

	@Test
	void shouldThrowWhenFilmNameIsBlank() {
		Film film = createValidFilm();
		film.setName(" ");

		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	void shouldThrowWhenDescriptionTooLong() {
		Film film = createValidFilm();
		film.setDescription("a".repeat(201));

		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	void shouldCreateFilmWithDescriptionLength200AndMinReleaseDate() {
		Film film = createValidFilm();
		film.setDescription("a".repeat(200));
		film.setReleaseDate(LocalDate.of(1895, 12, 28));

		Film created = filmController.create(film);

		assertEquals(200, created.getDescription().length());
		assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
	}

	@Test
	void shouldThrowWhenReleaseDateBeforeMin() {
		Film film = createValidFilm();
		film.setReleaseDate(LocalDate.of(1895, 12, 27));

		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	void shouldThrowWhenDurationNotPositive() {
		Film film = createValidFilm();
		film.setDuration(0);

		assertThrows(ValidationException.class, () -> filmController.create(film));
	}
}
