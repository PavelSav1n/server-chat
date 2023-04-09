package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import ru.itsjava.dao.MessageDao;
import ru.itsjava.dao.UserDao;
import ru.itsjava.domain.Message;
import ru.itsjava.domain.User;
import ru.itsjava.exceptions.MessagesNotFoundException;
import ru.itsjava.exceptions.RecipientNotFoundException;
import ru.itsjava.exceptions.UserExistsException;
import ru.itsjava.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


// Класс для многопоточности программы (multiple threads)
// Это сервис клиента -- (класс) для получения и отправки сообщения клиентам
// Метод run отвечает за принятие сообщений
// Метод notifyMe отвечает за отправку сообщений

@RequiredArgsConstructor
public class ClientRunnable implements Runnable, Observer {

    // Инициализация логера:
    private static final Logger log = Logger.getLogger(ClientRunnable.class);

    // Для работы с сокетами из сервиса ServerService, нужно задать поле. Для каждого экземпляра клиента оно своё:
    private final Socket socket;
    private final ServerService serverService;
    private User user; // Сущность (domain) клиента (пользователя)
    private final UserDao userDao; // для вызова метода проверки пользователя
    private final MessageDao messageDao; // для вызова метода проверки пользователя

    @SneakyThrows // чтобы не прокидывать исключения
    @Override
    public void run() {
        System.out.println("Client connected");
        log.info("Client " + this + " connected");

        // Считываем данные, отправленные клиентом.
        // Для этого необходимо создать объект для считывания потока, создаём поток, который будет читать данные с сокета
        // и вызываем на сокете метод getInputStream()
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String messageFromClient;
        int authFlag = 0;

        // *** Цикл приема сообщения из меню (авторизация/регистрация) ***
        while ((messageFromClient = bufferedReader.readLine()) != null) {
            // Добавляем клиента во временный список авторизации/регистрации для общения с сервером (если его там ещё нет)
            serverService.addObserverTemp(this);// this работает, потому что addObserverTemp ожидает на входе Observer, а у нас класс ServerService extends Observable

//            System.out.println("Добавили наблюдателя " + this.toString().substring(this.toString().length() - 9) + " во временный список"); //отладка
//            serverService.printAllObserversTemp(); // отладка
//            System.out.println(this + "messageFromClient = " + messageFromClient); // отладка

            // Авторизация / регистрация:
            if (messageFromClient.startsWith("!autho!")) {
                try {
                    log.info(messageFromClient);
                    user = authorization(messageFromClient); // получаем логин авторизированного пользователя
                    log.info("Client " + user.getName() + " authorized successfully");
                    authFlag = 1; // ставим флаг об успешной авторизации
                    serverService.notifyObserverTemp(this, "!auth success!"); // отправляем на клиента флаг об успешной авторизации
                } catch (UserNotFoundException e) { // ловим ошибку, если не нашли пользователя
                    log.error(" Authorization of " + this + " failed");
                    serverService.notifyObserverTemp(this, "!auth failed!");
                    serverService.deleteObserverTemp(this); // удаляем клиента из списка, потому что в начале цикла мы его снова добавим
                }
            } else if (messageFromClient.startsWith("!reg!")) {
                try {
                    log.info(messageFromClient);
                    user = registration(messageFromClient); // получаем логин зарегистрированного пользователя
                    System.out.println("... клиент " + user.getName() + " успешно зарегистрировался ...");
                    log.info("Client " + user.getName() + " registered successfully");
                    serverService.notifyObserverTemp(this, "!reg success!"); // отправляем на клиента флаг об успешной авторизации
                } catch (UserExistsException e) { // ловим ошибку, если пользователь уже есть
                    log.error(" Registration of " + this + " failed");
                    serverService.notifyObserverTemp(this, "!reg failed!");
                    serverService.deleteObserverTemp(this); // удаляем клиента из списка, потому что в начале цикла мы его снова добавим
                }
            }

            // флаг прохождения авторизации
            if (authFlag == 1) {
                // Как только авторизация пройдена, убираем клиента из списка авторизации и добавляем клиента в список observers:
                serverService.deleteObserverTemp(this);
                serverService.addObserver(this);

//                serverService.printAllObservers(); // отладка

                // *** Цикл приёма сообщения от клиентов ***
                while ((messageFromClient = bufferedReader.readLine()) != null) {
                    // Если пользователь выходит в меню, удаляем его из списка наблюдателей и выходим из цикла:
                    if (messageFromClient.equals("!exit")) {
                        log.info("User " + this.getUser() + " exited chat...");
                        serverService.notifyObserver(this, "!exit!");
                        serverService.deleteObserver(this);
                        authFlag = 0;
                        break;
                    }
                    // команда "!pm" -- private message
                    // !pm userName message
                    // Если это private message, то парсим адресат и отправляем ему:
                    if (messageFromClient.startsWith("!pm")) {
                        String recipientName = messageFromClient.substring(3).split(" ")[1]; // Имя адресата
                        User recipientUser = new User(recipientName); // адресат сообщения
                        String messageBody = messageFromClient.substring(3).split(" ", 3)[2]; // Тело сообщение после удаления !pm и UserName
                        try {
                            Observer recipientObserver = serverService.getObserverByName(recipientUser); // этот же пользователь в списке наблюдателей
                            serverService.notifyObserver(recipientObserver, messageBody); // пишем сообщение пользователю
                            Message message = new Message(user, recipientUser, messageBody); // создаём сообщение
                            System.out.println("message = " + message); // пишем сообщение в консоль
                            messageDao.WritePrivateMessageToDatabase(message); // пишем сообщение в БД

                        } catch (RecipientNotFoundException e) {
                            log.error(messageFromClient);
                            log.error("Recipient \"" + recipientName + "\" is not exists or online");
                            serverService.notifyObserver(this, "Recipient is not exists or online"); // пользователь может не существовать или не быть в списке
                        }
                    } else
                        // команда "!printLast X" печатает X последних сообщений
                        if (messageFromClient.startsWith("!printLast")) {
                            String amountOfMessages = messageFromClient.substring(10).split(" ")[1]; // количество последних сообщений для отображения (пока String, ниже будем конвертить в int)
                            try {
                                ArrayList<String> lastMessages = messageDao.getLastMessages(user, Integer.parseInt(amountOfMessages)); // конвертим в int
                                serverService.notifyObserver(this, "Printing last " + lastMessages.size() + " messages:");
                                for (int i = 0; i < lastMessages.size(); i++) {
                                    serverService.notifyObserver(this, lastMessages.get(i));
                                }
                            } catch (NumberFormatException | MessagesNotFoundException e) {
                                log.error(messageFromClient);
                                log.error("There is no messages, or try to enter an appropriate number");
                                serverService.notifyObserver(this, "There is no messages, or try to enter an appropriate number");
                            }
                        } else {
                            // Если нет адресата, то сообщение уходит всем:
                            Message message = new Message(user, messageFromClient); // создаём сообщение
                            messageDao.WritePublicMessageToDatabase(message); // пишем в БД
                            System.out.println(user.getName() + ": " + messageFromClient); // пишем в консоль сервера
                            // Уведомляем всех, кроме отправителя
                            serverService.notifyObserverExceptSender(this, user.getName() + ": " + messageFromClient);
                        }
                }
            }
        }
    }

    // Метод авторизации пользователя по String:
    // В случае успеха возвращает login пользователя
    // В случае провала -- UserNotFoundException()
    @SneakyThrows
    private User authorization(String authorizationMessage) {
        // Строка для авторизации:
        // !autho!login:password
        // начиная с 7 символа разбиваем подстроку на массив строк, по регулярному выражению ":"
        String login = authorizationMessage.substring(7).split(":")[0];
        String password = authorizationMessage.substring(7).split(":")[1];
        // Проверяем существует ли пользователь с таким логином и паролем (если нет, вылетим с ошибкой)
        if (userDao.findByNameAndPassword(login, password) != null) {
            return new User(login, password);
        }
        throw new UserNotFoundException();
    }

    // Метод регистрации пользователя по String:
    // В случае успеха возвращает login пользователя
    // В случае провала -- UserExistsException()
    @SneakyThrows
    private User registration(String registrationMessage) {
        // Строка для регистрации:
        // !reg!login:password
        // начиная с 5 символа разбиваем подстроку на массив строк, по регулярному выражению ":"
        String login = registrationMessage.substring(5).split(":")[0];
        String password = registrationMessage.substring(5).split(":")[1];
        // Проверяем существует ли пользователь с таким логином:
        if (userDao.findByName(login) == 0) {
            // Если нет, добавляем пользователя:
            userDao.createNewUser(login, password);
            return new User(login, password);
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

    // Получить имя данного Observer'а
    @Override
    public User getUser() {
        return this.user;
    }

}
