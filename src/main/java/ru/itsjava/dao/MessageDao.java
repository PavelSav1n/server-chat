package ru.itsjava.dao;

import ru.itsjava.domain.Message;

public interface MessageDao {

    Message WritePrivateMessageToDatabase (Message message);

    Message WritePublicMessageToDatabase (Message message);

}
