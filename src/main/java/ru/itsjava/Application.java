package ru.itsjava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Application {

    // Сначала реализуем логику получения сообщений через WebSocket
    // Поэтому пока реализация в main

    public final static int PORT = 8081;


    // Main Application
    public static void main(String[] args) throws IOException {
        // Создаём сокет:
        ServerSocket serverSocket = new ServerSocket(PORT);

        // Будем "слушать" этот порт:
        System.out.println(" === SERVER STARTS === ");
        while (true) {
            // Проверка, что клиент подключился
            // Создаём сокет и вызываем на ServerSocket метод accept()
            Socket socket = serverSocket.accept();
            // Проверяем, подключился ли клиент:
            if (socket != null){
                System.out.println("Client connected");
                // Считываем данные, отправленные клиентом.
                // Для этого необходимо создать объект для считывания потока, создаём поток, который будет читать данные с сокета
                // и вызываем на сокете метод getInputStream()
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("bufferedReader.readLine() = " + bufferedReader.readLine());

            }


        }
    }
}
