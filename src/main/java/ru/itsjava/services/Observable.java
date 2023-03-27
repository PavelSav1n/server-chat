package ru.itsjava.services;

public interface Observable {

    void addObserver(Observer observer); // добавить пользователя

    void deleteObserver(Observer observer); // удалить пользователя

    void notifyObserver(String message); // отправить уведомление всем наблюдателям

    void notifyObserverExceptSender(Observer observer, String message); // отправить уведомление всем, кроме отправителя

}
