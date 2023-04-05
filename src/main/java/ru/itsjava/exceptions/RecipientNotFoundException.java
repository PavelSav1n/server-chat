package ru.itsjava.exceptions;

public class RecipientNotFoundException extends RuntimeException{

    public RecipientNotFoundException() {
        super("Recipient not found in DB");
    }
}
