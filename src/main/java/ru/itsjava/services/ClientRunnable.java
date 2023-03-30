package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.dao.UserDao;
import ru.itsjava.domain.User;
import ru.itsjava.exceptions.UserExistsException;
import ru.itsjava.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Класс для многопоточности программы (multiple threads)
// Это сервис клиента -- (класс) для получения и отправки сообщения клиентам
// Метод run отвечает за принятие сообщений
// Метод notifyMe отвечает за отправку сообщений
@RequiredArgsConstructor
public class ClientRunnable implements Runnable, Observer {

    // Для работы с сокетами из сервиса ServerService, нужно задать поле. Для каждого экземпляра клиента оно своё:
    private final Socket socket;
    private final ServerService serverService;
    private User user; // Сущность (domain) клиента (пользователя) (пока не используем)
    private final UserDao userDao; // для вызова метода проверки пользователя

    @SneakyThrows // чтобы не прокидывать исключения
    @Override
    public void run() {
        System.out.println("Client connected");

        // Считываем данные, отправленные клиентом.
        // Для этого необходимо создать объект для считывания потока, создаём поток, который будет читать данные с сокета
        // и вызываем на сокете метод getInputStream()
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String messageFromClient;
        String userName = null;
        // *** Цикл приема сообщения из меню (авторизация/регистрация) ***
        while ((messageFromClient = bufferedReader.readLine()) != null) {
            if (messageFromClient.startsWith("!autho!")) {
                userName = authorization(messageFromClient); // получаем логин авторизированного пользователя
            } else if (messageFromClient.startsWith("!reg!")) {
                userName = registration(messageFromClient); // получаем логин зарегистрированного пользователя
            }
            if (userName != null) { // флаг прохождения регистрации или авторизации
                // Как только авторизация или регистрация пройдена, добавляем клиента в список observers:
                serverService.addObserver(this); // this работает, потому что addObserver ожидает на входе Observer, а у нас класс ServerService extends Observable
                // *** Цикл приёма сообщения от клиентов ***
                while ((messageFromClient = bufferedReader.readLine()) != null) {
                    // Если пользователь выходит в меню, удаляем его из списка наблюдателей и выходим из цикла:
                    if (messageFromClient.equals("exit")){
                        serverService.deleteObserver(this);
                        break;
                    }
                    System.out.println(userName + ": " + messageFromClient);
                    // Уведомляем всех, кроме отправителя
                    serverService.notifyObserverExceptSender(this, userName + ": " + messageFromClient);

                }
            }
        }
    }

    // Метод авторизации пользователя по BufferedReader:
    @SneakyThrows
    private boolean authorization(BufferedReader bufferedReader) {
        System.out.println("bufferedReader = " + bufferedReader); // отладка
        String authorizationMessage;
        while ((authorizationMessage = bufferedReader.readLine()) != null) {
            // Строка для авторизации:
            // !autho!login:password
            if (authorizationMessage.startsWith("!autho!")) {
                // начиная с 7 символа разбиваем подстроку на массив строк, по регулярному выражению ":"
                String login = authorizationMessage.substring(7).split(":")[0];
                String password = authorizationMessage.substring(7).split(":")[1];
                // Проверяем существует ли пользователь с таким логином и паролем (если нет, вылетим с ошибкой)
                user = userDao.findByNameAndPassword(login, password);
                return true;
            }
        }
        return false;
    }

    // Метод авторизации пользователя по String:
    // В случае успеха возвращает login пользователя
    // В случае провала -- UserNotFoundException()
    @SneakyThrows
    private String authorization(String authorizationMessage) {
        // Строка для авторизации:
        // !autho!login:password
        // начиная с 7 символа разбиваем подстроку на массив строк, по регулярному выражению ":"
        String login = authorizationMessage.substring(7).split(":")[0];
        String password = authorizationMessage.substring(7).split(":")[1];
        // Проверяем существует ли пользователь с таким логином и паролем (если нет, вылетим с ошибкой)
        if (userDao.findByNameAndPassword(login, password) != null) {
            return login;
        }
        throw new UserNotFoundException();
    }

    // Метод регистрации пользователя по BufferedReader:
    @SneakyThrows
    // 1 -- регистрация удалась
    // 0 -- регистрация не удалась
    private int registration(BufferedReader bufferedReader) {
        String registrationMessage;
        while ((registrationMessage = bufferedReader.readLine()) != null) {
            // Строка для регистрации:
            // !reg!login:password
            if (registrationMessage.startsWith("!reg!")) {
                // начиная с 5 символа разбиваем подстроку на массив строк, по регулярному выражению ":"
                String login = registrationMessage.substring(5).split(":")[0];
                String password = registrationMessage.substring(5).split(":")[1];
                // Проверяем существует ли пользователь с таким логином:
                if (userDao.findByName(login) == 0) {
                    // Если нет, добавляем пользователя:
                    userDao.createNewUser(login, password);
                    return 1;
                }
            }
        }
        return 0;
    }

    // Метод регистрации пользователя по String:
    // В случае успеха возвращает login пользователя
    // В случае провала -- UserExistsException()
    @SneakyThrows
    private String registration(String registrationMessage) {
        // Строка для регистрации:
        // !reg!login:password
        // начиная с 5 символа разбиваем подстроку на массив строк, по регулярному выражению ":"
        String login = registrationMessage.substring(5).split(":")[0];
        String password = registrationMessage.substring(5).split(":")[1];
        // Проверяем существует ли пользователь с таким логином:
        if (userDao.findByName(login) == 0) {
            // Если нет, добавляем пользователя:
            userDao.createNewUser(login, password);
            return login;
        }
        throw new UserExistsException();
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
