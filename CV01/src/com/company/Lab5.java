package com.company;

import java.util.Scanner;

public class Lab5 {
    void Start(String[] args) {

        ContactList contactList = new ContactList();
        Scanner sc = new Scanner(System.in);

        boolean contactCorrect = true;
        while(contactCorrect){
            System.out.println("Enter first name:");
            String name = sc.nextLine();
            System.out.println("Enter last name:");
            String surname = sc.nextLine();
            System.out.println("Enter telephone number:");
            String tel = sc.nextLine();
            System.out.println("Enter address:");
            String addr = sc.nextLine();
            try {
                Contact c = new Contact(name, surname, tel, addr);
                contactList.add(c);
            } catch (WrongNameException e) {
                System.out.println("Invalid name '" + e.getName() + "', contact not added.");
                contactCorrect = false;
            } catch (WrongTelephoneException e) {
                System.out.println("Invalid telephone number, contact not added.");
                contactCorrect = false;
            }
        }
        System.out.println("Contact list:");
        System.out.println(contactList);

        System.out.println("Ordered Contact List");
        contactList.sort(false);
        System.out.println(contactList);

        System.out.println("Reversed Ordered Contact List");
        contactList.sort(true);
        System.out.println(contactList);
    }
}
