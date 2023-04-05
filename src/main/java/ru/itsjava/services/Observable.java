package ru.itsjava.services;

import ru.itsjava.domain.User;

public interface Observable {

    void addObserver(Observer observer, User user); // добавить пользователя

    void deleteObserver(Observer observer); // удалить пользователя

    void notifyAllObservers(String message); // отправить уведомление всем наблюдателям

    void notifyObserverExceptSender(Observer observer, String message); // отправить уведомление всем, кроме отправителя

    void addObserverTemp(Observer observer); // добавить пользователя во временный список для авторизации

    void deleteObserverTemp(Observer observer); // удалить пользователя из временного списка для авторизации

    void notifyObserverTemp(Observer observer, String message); // отправить уведомление указанному наблюдателю во временном списке

    void notifyObserver(Observer observer, String message); // отправить уведомление указанному наблюдателю

    void printAllObservers();

    void printAllObserversTemp();

    Observer getObserverByName(User user); // получить наблюдателя по пользователю


}
