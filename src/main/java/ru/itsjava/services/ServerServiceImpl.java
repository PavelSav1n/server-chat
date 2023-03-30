package ru.itsjava.services;

import lombok.SneakyThrows;
import ru.itsjava.dao.UserDao;
import ru.itsjava.dao.UserDaoImpl;
import ru.itsjava.utils.Props;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Этот сервис работает с сокетами
// Всю кухню по чтению и записи текстовых сообщений мы переместили в ClientRunnable
public class ServerServiceImpl implements ServerService {

    private final static int PORT = 8081; // порт для открытия сокета
    private final List<Observer> observers = new ArrayList<>(); // список для хранения клиентов
    private final UserDao userDao = new UserDaoImpl(new Props()); // подключаем DAO работы с пользователем

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
                Thread thread = new Thread(new ClientRunnable(socket, this, userDao));
                thread.start(); // запускаем нить. Этот run() мы переопределили в ClientRunnable
            }
        }

    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    // Отправка сообщений всем наблюдателям в списке
    @Override
    public void notifyObserver(String message) {
        for (Observer elemObserver : observers) {
            elemObserver.notifyMe(message);
        }
    }

    // Отправка сообщений всем наблюдателям в списке, кроме отправителя
    @Override
    public void notifyObserverExceptSender(Observer observer, String message) {
        for (Observer elemObserver : observers) {
            if (!elemObserver.equals(observer)){
                elemObserver.notifyMe(message);
            }
        }
    }
}
