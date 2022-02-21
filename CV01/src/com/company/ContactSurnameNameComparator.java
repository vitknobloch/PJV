package com.company;

import java.util.Comparator;

public class ContactSurnameNameComparator implements Comparator<Contact> {

    @Override
    public int compare(Contact o1, Contact o2) {
        int ret = o1.surname.compareToIgnoreCase(o2.surname);
        if(ret == 0){
            ret = o1.name.compareToIgnoreCase(o2.name);
        }
        return ret;
    }
}
