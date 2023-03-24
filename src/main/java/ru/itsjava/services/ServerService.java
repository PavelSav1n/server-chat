package ru.itsjava.services;

// Расширяем этот интерфейс, интерфейсов Observable, для использования его методов.
public interface ServerService extends Observable {

    void start();
}
