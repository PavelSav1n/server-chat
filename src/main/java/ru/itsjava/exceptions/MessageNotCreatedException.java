package ru.itsjava.exceptions;

public class MessageNotCreatedException extends RuntimeException{

    public MessageNotCreatedException() {
        super("Message not created");
    }
}
