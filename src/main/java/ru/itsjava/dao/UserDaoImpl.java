package ru.itsjava.dao;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import ru.itsjava.domain.User;
import ru.itsjava.exceptions.UserExistsException;
import ru.itsjava.exceptions.UserNotFoundException;
import ru.itsjava.utils.Props;

import java.sql.*;

@Log4j
@AllArgsConstructor
public class UserDaoImpl implements UserDao {

    // Подгружаем настройки для использования данных оттуда для подключения к БД
    private final Props props;

    @Override
    // Создаём нового пользователя. Возвращаем пользователя, если успешно.
    public User createNewUser(String name, String password) {
        // Подключаемся к БД:
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"));
        ) {
            // Проверяем, есть ли подобный пользователь в БД. Если нет, добавляем:
            if (findByName(name) == 0) {
                // Готовим запрос:
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO schema_server.chat_users (login, password) values (?,?);");
                // Проставляем параметры вместо ?:
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, password);
                // Выполняем запрос:
                preparedStatement.executeUpdate();
                // Возвращаем успешно созданного пользователя:
                return new User(name, password);
            }
        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
        }
        throw new UserExistsException(); // пользователь уже есть с таким именем
    }

    @Override
    // Удаляем пользователя по логину и паролю. Возвращаем удалённого пользователя.
    public User deleteUser(String name, String password) {
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"));
        ) {
            // Проверяем, есть ли подобный пользователь в БД. Если есть, удаляем:
            if (findByName(name) == 1) {
                // Готовим запрос:
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM schema_server.chat_users WHERE login=? and password = ?;");
                // Проставляем параметры вместо ?:
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, password);
                // Выполняем запрос:
                preparedStatement.executeUpdate(); // 1 -- если выполнили
                // Возвращаем удалённого пользователя:
                return new User(name, password);
            }
        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
        }
        throw new UserNotFoundException(); // не нашли пользователя
    }

    // Поиск по имени. Если нашли, то 1, если нет, 0
    @Override
    public int findByName(String name) {
        // Подключаемся к БД:
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"));
        ) {
            // Готовим запрос:
            PreparedStatement preparedStatement = connection.prepareStatement("select count(*) cnt from schema_server.chat_users where login=?;");
            // Проставляем параметры вместо ?:
            preparedStatement.setString(1, name);
            // Выполняем запрос:
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            // Выбираем из результата запроса первое значение колонки cnt (0 или 1 в зависимости от того, есть ли пользователь с таким именем):
            int userCount = resultSet.getInt("cnt");
            // Если совпадение есть, то возвращаем 1:
            if (userCount == 1) {
                return 1;
            }
        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
        }
        return 0; // не нашли такого пользователя
    }

    @Override
    // Вернуть пользователя по логину и паролю. Если пары нет, кидает RuntimeException
    public User findByNameAndPassword(String name, String password) {
        // Подключаемся к БД:
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"));
        ) {
            // Готовим запрос:
            PreparedStatement preparedStatement = connection.prepareStatement("select count(*) cnt from schema_server.chat_users where login=? and password = ?;");
            // Проставляем параметры вместо ?:
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);
            // Выполняем запрос:
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            // Выбираем из результата запроса первое значение колонки cnt (0 или 1 в зависимости от того, есть ли пользователь с парой или нет):
            int userCount = resultSet.getInt("cnt");
            // Если совпадение есть, то возвращаем экземпляр пользователя с логином и паролем:
            if (userCount == 1) {
                return new User(name, password);
            }
        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
        }
        throw new UserNotFoundException();
    }


}
