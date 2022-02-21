package com.company;

public class Main {

    public static void main(String[] args) {
	    // write your code here
        LAB08 lab = new LAB08();

        lab.LoadCSV("C:\\Users\\vitak\\ownCloud\\PJV\\CVIKA\\tel_seznam_KUp_2.csv");

        lab.saveObj("out.bin");
        lab.loadObj("out.bin");

        lab.saveCSV("out.csv");
    }
}