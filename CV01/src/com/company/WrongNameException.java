package com.company;

public class WrongNameException extends Exception{
    String name;
    public WrongNameException(String message, String name) {
        super(message);
        name = name;
    }

    public String getName(){
        return name;
    }
}
