package ru.itsjava.dao;

import ru.itsjava.domain.User;

public interface UserDao {

    User findByNameAndPassword(String name, String password);

    int findByName(String name);

    User createNewUser(String name, String password);

    User deleteUser(String name, String password);

}
