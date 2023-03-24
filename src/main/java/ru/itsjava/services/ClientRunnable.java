package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Это сервис клиента -- (класс) для получения и отправки сообщения клиентам
// Метод run отвечает за принятие сообщений
// Метод notifyMe отвечает за отправку сообщений
@RequiredArgsConstructor // для private final Socket socket;
public class ClientRunnable implements Runnable, Observer {

    // Для работы с сокетами из сервиса ServerService, нужно задать поле. Для каждого экземпляра клиента оно своё:
    private final Socket socket;
    private final ServerService serverService;

    @SneakyThrows // чтобы не прокидывать исключения
    @Override
    public void run() {
        System.out.println("Client connected");
        // Как только был вызван метод run() -- это значит, что клиент существует, поэтому добавляем его в список клиентов:
        serverService.addObserver(this); // this работает, потому что addObserver ожидает на входе Observer, а у нас класс implements Observer
        // Считываем данные, отправленные клиентом.
        // Для этого необходимо создать объект для считывания потока, создаём поток, который будет читать данные с сокета
        // и вызываем на сокете метод getInputStream()
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String messageFromClient;
        while ((messageFromClient = bufferedReader.readLine()) != null) {
            System.out.println(messageFromClient);
            // Для того чтобы всем отправилось сообщение от клиента, необходимо чтобы список клиентов был не пуст.
            // Метод notifyObserver возможно использовать отсюда, поскольку interface ServerService extends Observable
            serverService.notifyObserver(messageFromClient);
        }
    }

    @SneakyThrows
    @Override
    public void notifyMe(String message) {
        PrintWriter clientWriter = new PrintWriter(socket.getOutputStream());
        clientWriter.println(message);
        clientWriter.flush();
    }
}
