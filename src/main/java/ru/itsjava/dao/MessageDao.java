package ru.itsjava.dao;

import ru.itsjava.domain.Message;
import ru.itsjava.domain.User;

import java.util.ArrayList;

public interface MessageDao {

    Message WritePrivateMessageToDatabase(Message message);

    Message WritePublicMessageToDatabase(Message message);

    ArrayList<String> getLastMessages(User user, int amount); // получить amount сообщений из БД, за исключением приватных другим пользователям

    ArrayList<String> getAllMessages(User user); // получить все сообщения из БД, за исключением приватных другим пользователям



}
