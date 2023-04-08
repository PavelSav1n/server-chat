package ru.itsjava.exceptions;

public class MessagesNotFoundException extends RuntimeException {

    public MessagesNotFoundException() {
        super("Suitable messages are not found in DB");
    }
}
