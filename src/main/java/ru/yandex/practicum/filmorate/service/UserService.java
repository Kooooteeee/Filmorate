package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(long userId, long friendId) {
        log.info("PUT /users/{}/friends/{}", userId, friendId);

        userStorage.findById(userId);
        userStorage.findById(friendId);

        userStorage.addFriend(userId, friendId);

        return userStorage.findById(userId);
    }

    public User removeFriend(long userId, long friendId) {
        log.info("DELETE /users/{}/friends/{}", userId, friendId);

        userStorage.findById(userId);
        userStorage.findById(friendId);

        userStorage.removeFriend(userId, friendId);

        return userStorage.findById(userId);
    }

    public Collection<User> getUserFriends(long userId) {
        log.info("GET /users/{}/friends", userId);

        userStorage.findById(userId);
        return userStorage.findFriends(userId);
    }

    public Collection<User> getCommonFriends(long userId, long otherId) {
        log.info("GET /users/{}/friends/common/{}", userId, otherId);

        userStorage.findById(userId);
        userStorage.findById(otherId);

        return userStorage.findCommonFriends(userId, otherId);
    }
}
