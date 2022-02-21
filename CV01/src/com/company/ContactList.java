package com.company;

import java.util.ArrayList;

public class ContactList {
    ArrayList<Contact> list;

    public ContactList() {
        this.list = new ArrayList<>();
    }

    public void add(Contact c){
        list.add(c);
    }

    @Override
    public String toString(){
        String ret = "";
        for (Contact c : list) {
            ret += c.toString() + "\n";
        }
        return ret;
    }

    public void sort(boolean reverse){
        if(reverse)
            list.sort(new ContactSurnameNameComparator().reversed());
        else
            list.sort(new ContactSurnameNameComparator());
    }
}
