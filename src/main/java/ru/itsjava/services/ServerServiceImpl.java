package ru.itsjava.services;

import lombok.SneakyThrows;
import ru.itsjava.dao.MessageDao;
import ru.itsjava.dao.MessageDaoImpl;
import ru.itsjava.dao.UserDao;
import ru.itsjava.dao.UserDaoImpl;
import ru.itsjava.domain.User;
import ru.itsjava.exceptions.RecipientNotFoundException;
import ru.itsjava.utils.Props;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Этот сервис работает с сокетами
// Всю кухню по чтению и записи текстовых сообщений мы переместили в ClientRunnable
public class ServerServiceImpl implements ServerService {

    private final static int PORT = 8081; // порт для открытия сокета
    private final Map<Observer, User> observers = new HashMap(); // список для хранения клиентов
    private final Set<Observer> observersTemp = new HashSet<>(); // список для временного хранения клиентов пока они авторизуются (чтобы сервер мог с ними общаться)
    private final UserDao userDao = new UserDaoImpl(new Props()); // подключаем DAO работы с пользователем
    private final MessageDao messageDao = new MessageDaoImpl(new Props()); // подключаем DAO для работы с сообщениями

    @SneakyThrows // чтобы не прокидывать исключения
    @Override
    public void start() {

        // Создаём сокет:
        ServerSocket serverSocket = new ServerSocket(PORT);

        // Будем "слушать" этот порт:
        System.out.println(" === SERVER STARTS === ");
        while (true) {
            // Проверка, что клиент подключился
            // Создаём сокет и вызываем на ServerSocket метод accept()
            Socket socket = serverSocket.accept();
            // Проверяем, подключился ли клиент:
            if (socket != null) {
                // Создаём новую нить для каждого отдельного клиента
                Thread thread = new Thread(new ClientRunnable(socket, this, userDao, messageDao));
                thread.start(); // запускаем нить. Этот run() мы переопределили в ClientRunnable
            }
        }

    }

    @Override
    public void addObserver(Observer observer, User user) {
        observers.put(observer, user);
    }

    @Override
    public void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    // Отправка сообщений всем наблюдателям в списке
    @Override
    public void notifyAllObservers(String message) {
        for (Observer elemObserver : observers.keySet()) {
            elemObserver.notifyMe(message);
        }
    }

    // Отправка сообщений всем наблюдателям в списке, кроме отправителя
    @Override
    public void notifyObserverExceptSender(Observer observer, String message) {
        for (Observer elemObserver : observers.keySet()) {
            if (!elemObserver.equals(observer)) {
                elemObserver.notifyMe(message);
            }
        }
    }

    // Добавляет наблюдателя во временный список авторизации/регистрации, если его ещё там нет
    @Override
    public void addObserverTemp(Observer observer) {
        // При использовании ArrayList столкнулся с ошибкой Concurrent...Exception при запуске двух клиентов и выполнения кода ниже:
        // Нужны были уникальные записи в списке, поэтому перешёл на HashSet

        // Если список пустой, просто добавляем:
//        if (observersTemp.size() == 0) {
//            observersTemp.add(observer);
//        } else {
//            // Если список не пуст, то если нет совпадений, добавляем:
//            for (Observer elemObserver : observersTemp) {
//                if (!elemObserver.equals(observer)) {
        observersTemp.add(observer);
//                }
//            }
//        }
    }

    @Override
    public void deleteObserverTemp(Observer observer) {
        observersTemp.remove(observer);
    }

    // Отправка сообщений указанному пользователю во временном списке авторизации
    @Override
    public void notifyObserverTemp(Observer observer, String message) {
        for (Observer elemObserver : observersTemp) {
            if (elemObserver.equals(observer)) {
                elemObserver.notifyMe(message);
            }
        }
    }

    // Отправка сообщений указанному пользователю
    @Override
    public void notifyObserver(Observer observer, String message) {
        for (Observer elemObserver : observers.keySet()) {
            if (elemObserver.equals(observer)) {
                elemObserver.notifyMe(message);
            }
        }
    }

    // Выводим список всех наблюдателей в hashMap() observersTemp
    @Override
    public void printAllObservers() {
        for (Map.Entry<Observer, User> elemMap : observers.entrySet()){
            System.out.println(elemMap);
        }

    }

    // Выводим список всех наблюдателей в hashSet() observersTemp
    @Override
    public void printAllObserversTemp() {
        int count = 0;
        for (Observer elemObs : observersTemp) {
            System.out.println(++count + ": " + elemObs.toString().substring(elemObs.toString().length() - 9)); // для удобства восприятия оставил только hash
        }

    }

    // Получаем наблюдателя по имени
    @Override
    public Observer getObserverByName(User user) {
        for (Map.Entry<Observer, User> elemMap : observers.entrySet()) {
            if (elemMap.getValue().getName().equals(user.getName())) { // сравниваем по значению поля name, а не по объектам потому что в user.equals() сравниваются ещё поля паролей
                return elemMap.getKey();
            }
        }
        throw new RecipientNotFoundException();
    }
}
