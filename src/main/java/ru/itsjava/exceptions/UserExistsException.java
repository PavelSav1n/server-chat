package ru.itsjava.exceptions;

public class UserExistsException extends RuntimeException{

    public UserExistsException (){
        super ("User already exists in database");
    }
}
