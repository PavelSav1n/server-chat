package ru.itsjava.services;

import lombok.SneakyThrows;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Этот сервис работает с сокетами
// Всю кухню по чтению и записи текстовых сообщений мы переместили в ClientRunnable
public class ServerServiceImpl implements ServerService, Observable {

    public final static int PORT = 8081; // порт для открытия сокета
    public final List<Observer> observers = new ArrayList<>(); // список для хранения клиентов

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
                // Создаём новый поток для каждого отдельного клиента
                Thread thread = new Thread(new ClientRunnable(socket));
                thread.start(); // запускаем поток

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

    @Override
    public void notifyObserver(String message) {
        for (Observer elemObserver : observers) {
            elemObserver.notifyMe(message);
        }
    }
}
