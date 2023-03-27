package ru.itsjava.services;

public interface Observer {

    void notifyMe(String message); // Подписчик может уведомлять себя. Только не понятно зачем.
    // как выяснилось, этот метод нужен для принятия сообщения от сервера.
}
