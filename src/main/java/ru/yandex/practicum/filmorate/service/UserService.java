package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(long id, long friendId) {
        log.info("Получен запрос PUT /users/{id}/friends/{friendId}");
        if (userStorage.findById(id) == null || userStorage.findById(friendId) == null) {
            log.error("Один из пользователей или оба не существуют id1={}, id2={}", id, friendId);
            throw new NotFoundException("Один из пользователей не найден, или не найдены оба.");
        }

        User user = userStorage.findById(id);
        user.addFriend(friendId);

        userStorage.findById(friendId).addFriend(id);
        log.info("В друзья пользователю {} добавлен {}", user, userStorage.findById(friendId));
        return user;
    }

    public User removeFriend(long id, long friendId) {
        log.info("Получен запрос DELETE /users/{id}/friends/{friendId}");
        if (userStorage.findById(id) == null || userStorage.findById(friendId) == null) {
            log.error("Один из пользователей или оба не существуют id1={}, id2={}", id, friendId);
            throw new NotFoundException("Один из пользователей не найден.");
        }

        User user = userStorage.findById(id);

        user.removeFriend(friendId);
        userStorage.findById(friendId).removeFriend(id);

        log.info("Из друзей пользователю {} удален {}", user, userStorage.findById(friendId));
        return user;
    }

    public Collection<User> getUserFriends(long id) {
        log.info("Получен запрос GET /users/{id}/friends");
        if (userStorage.findById(id) == null) {
            log.error("Пользователь {} не существует.", userStorage.findById(id));
            throw new NotFoundException("Пользователь не найден.");
        }

        User user = userStorage.findById(id);
        List<User> friends = user.getFriendsId().stream()
                .map(userStorage::findById)
                .toList();

        log.info("Получен список друзей пользователя {}: {}", user, friends);
        return friends;
    }

    public Collection<User> getCommonFriends(long id, long otherId) {
        log.info("Получен запрос GET /users/{id}/friends/common/{otherId}");
        if (userStorage.findById(id) == null || userStorage.findById(otherId) == null) {
            log.error("Один из пользователей или оба не существуют id1={}, id2={}", id, otherId);
            throw new NotFoundException("Один из пользователей не найден.");
        }

        User user = userStorage.findById(id);
        User otherUser = userStorage.findById(otherId);

        List<User> friends = user.getFriendsId().stream()
                .filter(i -> otherUser.getFriendsId().contains(i))
                .map(userStorage::findById)
                .toList();
        log.info("Получены общие друзья пользователей {} и {}: {}", user, otherUser, friends);
        return friends;
    }
}
