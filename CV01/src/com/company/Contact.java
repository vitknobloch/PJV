package com.company;

import java.lang.constant.Constable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contact{
    String name;
    String surname;
    String tel;
    String address;

    public Contact(String name, String surname, String tel, String address) throws WrongNameException, WrongTelephoneException {
        setName(name);
        setSurname(surname);
        setTel(tel);

        this.address = address;
    }

    public void setName(String name) throws WrongNameException {
        if(name.length() < 2){
            throw new WrongNameException("Incorrect name", name);
        }
        this.name = name;
    }

    public void setSurname(String surname) throws  WrongNameException{
        if(surname.length() < 2){
            throw new WrongNameException("Incorrect name", surname);
        }
        this.surname = surname;
    }

    public void setTel(String tel) throws WrongTelephoneException {
        String regex = "(\\d\\s?)*";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(tel);
        if(!m.matches()){
            throw new WrongTelephoneException("Incorrect telephone number");
        }
        this.tel = tel;
    }

    @Override
    public String toString(){
        return String.format("%s %s, tel: %s, addr: %s", name, surname, tel, address);
    }
}
