package cz.cvut.fel.pjv;

import java.util.Scanner;

public class Lab01 {
   
   public void start(String[] args) {
     homework();
   }

   private static void homework() {
      Scanner sc = new Scanner(System.in);

      //Get operation
      System.out.println("Vyber operaci (1-soucet, 2-rozdil, 3-soucin, 4-podil):");
      int op = sc.nextInt();
      if(op < 1 || op > 4){
         System.out.println("Chybna volba!");
         return;
      }

      //init result variables
      char opChar = ' ';
      double[] nums = {0, 0};
      double result = 0f;
      boolean zeroDiv = false;

      //perform operation
      switch (op){
         case 1:
            nums = getTwoDoubles(sc, "scitanec", "scitanec");
            result = nums[0] + nums[1];
            opChar = '+';
            break;
         case 2:
            nums = getTwoDoubles(sc, "mensenec", "mensitel");
            result = nums[0] - nums[1];
            opChar = '-';
            break;
         case 3:
            nums = getTwoDoubles(sc, "cinitel", "cinitel");
            result = nums[0] * nums[1];
            opChar = '*';
            break;
         case 4:
            nums = getTwoDoubles(sc, "delenec", "delitel");
            if(nums[1] == 0f){
               zeroDiv = true;
               break;
            }
            result = nums[0] / nums[1];
            opChar = '/';
            break;
      }

      //catch zero division
      if(zeroDiv){
         System.out.println("Pokus o deleni nulou!");
         return;
      }

      //Get decimal digit count
      System.out.println("Zadej pocet desetinnych mist: ");
      int decimalDigits = sc.nextInt();
      if(decimalDigits < 0){
         System.out.println("Chyba - musi byt zadane kladne cislo!");
         return;
      }

      //print out result
      String formatStr = String.format("%%.%1$df %2$c %%.%1$df = %%.%1$df%%n", decimalDigits, opChar);
      System.out.printf(formatStr, nums[0], nums[1], result);

   }

   private static double[] getTwoDoubles(Scanner sc, String name1, String name2){
      double[] ret = new double[2];
      System.out.printf("Zadej %s: %n", name1);
      ret[0] = sc.nextDouble();
      System.out.printf("Zadej %s: %n", name2);
      ret[1] = sc.nextDouble();

      return ret;
   }
}