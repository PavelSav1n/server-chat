package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Класс для многопоточности программы (multiple threads)
// Это сервис клиента -- (класс) для получения и отправки сообщения клиентам
// Метод run отвечает за принятие сообщений
// Метод notifyMe отвечает за отправку сообщений
@RequiredArgsConstructor // для private final Socket socket;
public class ClientRunnable implements Runnable, Observer {

    // Для работы с сокетами из сервиса ServerService, нужно задать поле. Для каждого экземпляра клиента оно своё:
    private final Socket socket;
    private final ServerService serverService;
    // Сущность (domain) клиента (пользователя):
    private User user;

    @SneakyThrows // чтобы не прокидывать исключения
    @Override
    public void run() {
        System.out.println("Client connected");


        // Считываем данные, отправленные клиентом.
        // Для этого необходимо создать объект для считывания потока, создаём поток, который будет читать данные с сокета
        // и вызываем на сокете метод getInputStream()
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String messageFromClient;
        // Проводим авторизацию пользователя по введённой строке:
        if (authorization(bufferedReader)) {
            // Как только авторизация пройдена, добавляем клиента в список observers:
            serverService.addObserver(this); // this работает, потому что addObserver ожидает на входе Observer, а у нас класс ServerService extends Observable
            while ((messageFromClient = bufferedReader.readLine()) != null) {
                System.out.println(user.getName() + ": " + messageFromClient);
                // Метод notifyObserver возможно использовать отсюда, поскольку interface ServerService extends Observable
                // Уведомляем всех, кроме отправителя
                serverService.notifyObserverExceptSender(this, user.getName() + ": " + messageFromClient);
            }
        }
    }

    // Метод авторизации пользователя:
    @SneakyThrows
    private boolean authorization(BufferedReader bufferedReader) {
        String authorizationMessage;
        while ((authorizationMessage = bufferedReader.readLine()) != null) {
            // Строка для авторизации:
            // !autho!login:password
            if (authorizationMessage.startsWith("!autho!")) {
                // начиная с 7 символа разбиваем подстроку на массив строк, по регулярному выражению ":"
                String login = authorizationMessage.substring(7).split(":")[0];
                String password = authorizationMessage.substring(7).split(":")[1];
                user = new User(login, password);
                return true;
            }
        }
        return false;
    }

    // Отправка сообщения клиенту
    @SneakyThrows
    @Override
    public void notifyMe(String message) {
        PrintWriter clientWriter = new PrintWriter(socket.getOutputStream());
        clientWriter.println(message);
        clientWriter.flush();
    }

}
