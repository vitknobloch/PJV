package com.company;

import java.util.Arrays;
import java.util.Scanner;

public class Lab1 {

    Scanner sc = new Scanner(System.in);

    void Start(String[] args) {

        int[][] rand = task10(5, 10, 10, 30);
        task11(rand);
    }

    void task2(){
        System.out.println("Zadejte polomer: ");
        double r = sc.nextDouble();
        System.out.printf("Obvod:  %.3f%n", 2*Math.PI*r);
        System.out.printf("Obsah: %.3f%n", Math.PI * r * r);
    }

    void task3() {
        System.out.println("Zadej cislo dne v tydnu:");
        int day;
        day = sc.nextInt();

        switch (day) {
            case 1:
                System.out.println("Monday");
                break;
            case 2:
                System.out.println("Tuesday");
                break;
        }
    }

    void task4(){
        int num;
        int[] lastTwo = new int[2];
        int[] highestTwo = new int[2];
        int sum = 0;
        int count = 0;

        boolean constant = true;
        boolean rising = true;
        boolean falling = true;
        boolean notrising = true;
        boolean notfalling = true;

        while((num = sc.nextInt()) != 0){
            lastTwo[1] = lastTwo[0];
            lastTwo[0] = num;

            if(lastTwo[0] > lastTwo[1]){
                notrising = false;
                falling = false;
                constant = false;
            }
            else if(lastTwo[1] > lastTwo[0]){
                notfalling = false;
                rising = false;
                constant = false;
            }
            else if(lastTwo[0] == lastTwo[1]){
                rising = false;
                falling = false;
            }

            if(num >= highestTwo[0]){
                highestTwo[1] = highestTwo[0];
                highestTwo[0] = num;
            }
            else if(num >= highestTwo[1]){
                highestTwo[1] = num;
            }

            sum += num;
            count++;
        }

        if(count == 0){
            System.out.println("Nelze spocitat prumer");
        }else{
            System.out.printf("Prumer: %.4f%n", sum/(double)count);

        }

        if(count > 1){
            System.out.printf("Druhy nejvetsi prvek: %d%n", highestTwo[1]);
        }else {
            System.out.println("Nelze vypsat druhý největší prvek");
        }

        if(constant){
            System.out.println("Rada je konstanti");
        }

        if(rising){
            System.out.println("Rada je rostouci");
        }else if(notfalling){
            System.out.println("Rada je neklesajici");
        }

        if(falling){
            System.out.println("Rada je klesajici");
        }else if(notrising){
            System.out.println("Rada je nerostouci");
        }
    }

    String task5(int num){
        String res = "" + num + " = ";

        int count = 0;
        int max = (int)Math.ceil(Math.sqrt(num));
        for(int i = 2; i <= max; i++){
            if(num % i == 0){

                num /= i;
                count++;
                if(count > 1){
                    res += "*";
                }
                res += i;
                i--;
            }
            if(num == 1)
                break;
        }
        if(count == 0){
            res += num;
        }
        return res;
    }

    void task6(int[] arr){
        for(int i = 0; i < arr.length; i++){
            System.out.printf("%d ", arr[i]);
        }
        System.out.println();
    }

    int[] task8(int len, int min, int max){
        int[] ret = new int[len];
        for(int i = 0; i < len; i++){
            double r = Math.random();
            ret[i] = min + (int)Math.round((max - min) * r);
        }
        return ret;
    }

    void bubbleSort(int arr[]){
        boolean sorted = false;
        while(!sorted){
            sorted = true;
            for(int i = 0; i < arr.length - 1; i++){
                if(arr[i] > arr[i+1]){
                    int temp = arr[i+1];
                    arr[i+1] = arr[i];
                    arr[i] = temp;
                    sorted = false;
                }
            }
        }
    }

    int[][] task10(int len1, int len2, int min, int max){
        int[][] ret = new int[len1][];
        for(int i = 0; i < len1; i++){
            ret[i] = task8(len2, min, max);
        }
        return ret;
    }

    void task11(int[][] arr){
        for(int i = 0; i < arr.length; i++){
            task6(arr[i]);
        }
    }
}
