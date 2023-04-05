package ru.itsjava.dao;

import lombok.RequiredArgsConstructor;
import ru.itsjava.domain.Message;
import ru.itsjava.exceptions.MessageNotCreatedException;
import ru.itsjava.exceptions.RecipientNotFoundException;
import ru.itsjava.utils.Props;

import java.sql.*;

@RequiredArgsConstructor
public class MessageDaoImpl implements MessageDao {

    // Подгружаем настройки для использования данных оттуда для подключения к БД
    private final Props props;

    @Override
    public Message WritePrivateMessageToDatabase(Message message) {
        // Подключаемся к БД:
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"));
        ) {
            // Извлекаем из таблицы id отправителя:
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM schema_online_course.chat_users WHERE login = ?;");
            preparedStatement.setString(1, message.getFrom().toString()); // в domain User прописан toString чтобы возвращал только имя
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int fromId = resultSet.getInt("id");

            // Извлекаем из таблицы id получателя:
            preparedStatement = connection.prepareStatement("SELECT id FROM schema_online_course.chat_users WHERE login = ?;");
            preparedStatement.setString(1, message.getTo().toString()); // в domain User прописан toString чтобы возвращал только имя
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int toId = resultSet.getInt("id");

            // Готовим запрос:
            preparedStatement = connection.prepareStatement("INSERT INTO schema_online_course.chat_messages (sender, recipient, text) VALUES (?,?,?);");
            // Проставляем параметры вместо ?:
            preparedStatement.setInt(1, fromId);
            preparedStatement.setInt(2, toId);
            preparedStatement.setString(3, message.getText());
            // Выполняем запрос:
            System.out.println("Executing inserting message into chat_messages table ... = " + preparedStatement.executeUpdate());
            // Возвращаем успешно записанное сообщение:
            return new Message(message.getFrom(), message.getTo(), message.getText());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RecipientNotFoundException(); // не нашли пользователя в ДБ
    }

    @Override
    public Message WritePublicMessageToDatabase(Message message) {
        // Подключаемся к БД:
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"));
        ) {

            // Извлекаем из таблицы id отправителя:
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM schema_online_course.chat_users WHERE login = ?;");
            preparedStatement.setString(1, message.getFrom().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int fromId = resultSet.getInt("id");

            // Готовим запрос:
            preparedStatement = connection.prepareStatement("INSERT INTO schema_online_course.chat_messages (sender, text) VALUES (?,?);");
            // Проставляем параметры вместо ?:
            preparedStatement.setInt(1, fromId);
            preparedStatement.setString(2, message.getText());
            // Выполняем запрос:
            System.out.println("Executing inserting message into chat_messages table ... = " + preparedStatement.executeUpdate());
            // Возвращаем успешно записанное сообщение:
            return new Message(message.getFrom(), message.getText());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new MessageNotCreatedException(); // На всякий случай
    }
}
