package ru.itsjava.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import ru.itsjava.domain.Message;
import ru.itsjava.domain.User;
import ru.itsjava.exceptions.MessageNotCreatedException;
import ru.itsjava.exceptions.MessagesNotFoundException;
import ru.itsjava.exceptions.RecipientNotFoundException;
import ru.itsjava.utils.Props;
import java.sql.*;
import java.util.ArrayList;

@Log4j
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
                props.getValue("db.password"))
        ) {
            // Извлекаем из таблицы id отправителя:
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM schema_server.chat_users WHERE login = ?;");
            preparedStatement.setString(1, message.getFrom().toString()); // в domain User прописан toString чтобы возвращал только имя
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int fromId = resultSet.getInt("id");

            // Извлекаем из таблицы id получателя:
            preparedStatement = connection.prepareStatement("SELECT id FROM schema_server.chat_users WHERE login = ?;");
            preparedStatement.setString(1, message.getTo().toString()); // в domain User прописан toString чтобы возвращал только имя
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int toId = resultSet.getInt("id");

            // Готовим запрос:
            preparedStatement = connection.prepareStatement("INSERT INTO schema_server.chat_messages (sender, recipient, text) VALUES (?,?,?);");
            // Проставляем параметры вместо ?:
            preparedStatement.setInt(1, fromId);
            preparedStatement.setInt(2, toId);
            preparedStatement.setString(3, message.getText());
            // Выполняем запрос:
            preparedStatement.executeUpdate();
            // Возвращаем успешно записанное сообщение:
            return new Message(message.getFrom(), message.getTo(), message.getText());

        } catch (SQLException e) {
            log.error(e);
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
                props.getValue("db.password"))
        ) {
            // Извлекаем из таблицы id отправителя:
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM schema_server.chat_users WHERE login = ?;");
            preparedStatement.setString(1, message.getFrom().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int fromId = resultSet.getInt("id");

            // Готовим запрос:
            preparedStatement = connection.prepareStatement("INSERT INTO schema_server.chat_messages (sender, text) VALUES (?,?);");
            // Проставляем параметры вместо ?:
            preparedStatement.setInt(1, fromId);
            preparedStatement.setString(2, message.getText());
            // Выполняем запрос:
            preparedStatement.executeUpdate();
            // Возвращаем успешно записанное сообщение:
            return new Message(message.getFrom(), message.getText());

        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
        }
        throw new MessageNotCreatedException(); // На всякий случай
    }


    // получить amount сообщений из БД, за исключением приватных другим пользователям
    @Override
    public ArrayList<String> getLastMessages(User user, int amount) {

        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"))
        ) {
            PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT * FROM (\n" +
                    "SELECT msg.id as messageID, sender.login as senderName, receiver.login as receiverName, msg.text\n" +
                    "FROM schema_server.chat_messages AS msg\n" +
                    "INNER JOIN schema_server.chat_users AS sender ON msg.sender = sender.id\n" +
                    "LEFT JOIN schema_server.chat_users AS receiver ON msg.recipient = receiver.id\n" +
                    "WHERE (recipient IS NULL OR receiver.login=?)\n" +
                    "ORDER BY msg.id DESC\n" +
                    "LIMIT ?) UnsortedResult\n" +
                    "ORDER BY messageID ASC;");

            preparedStatement.setString(1, user.getName());
            preparedStatement.setInt(2, amount);
            ResultSet resultSet = preparedStatement.executeQuery();

            ArrayList<String> messageArray = new ArrayList<>();

            while(resultSet.next()) {
                if (resultSet.getString("receiverName") == null) {
                    messageArray.add(resultSet.getString("senderName") + ":  " + resultSet.getString("text"));
                } else {
                    messageArray.add(resultSet.getString("senderName") + " to " + resultSet.getString("receiverName") + ": " + resultSet.getString("text"));
                }
            }
            return messageArray;

        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
        }
        throw new MessagesNotFoundException();
    }

    // получить все сообщения из БД, за исключением приватных другим пользователям
    @Override
    public ArrayList<String> getAllMessages(User user) {

        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"))
        ) {
            PreparedStatement preparedStatement = connection.prepareStatement("" +
                    "SELECT sender.login as senderName, receiver.login as receiverName, msg.text\n" +
                    "FROM schema_server.chat_messages AS msg\n" +
                    "INNER JOIN schema_server.chat_users AS sender ON msg.sender = sender.id\n" +
                    "LEFT JOIN schema_server.chat_users AS receiver ON msg.recipient = receiver.id\n" +
                    "WHERE (recipient IS NULL OR receiver.login=?);");

            preparedStatement.setString(1, user.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            ArrayList<String> messageArray = new ArrayList<>();

            while(resultSet.next()) {
                if (resultSet.getString("receiverName") == null) {
                    messageArray.add(resultSet.getString("senderName") + ":  " + resultSet.getString("text"));
                } else {
                    messageArray.add(resultSet.getString("senderName") + " to " + resultSet.getString("receiverName") + ": " + resultSet.getString("text"));
                }
            }
            return messageArray;

        } catch (SQLException e) {
            log.error(e);
            e.printStackTrace();
        }
        throw new MessagesNotFoundException();
    }
}
