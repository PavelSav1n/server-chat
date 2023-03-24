package ru.itsjava;


import ru.itsjava.services.ServerService;
import ru.itsjava.services.ServerServiceImpl;

import java.io.IOException;


public class Application {

    // Main Application
    public static void main(String[] args) throws IOException {

        ServerService serverService = new ServerServiceImpl();
        serverService.start();

    }
}
