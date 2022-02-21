/*
 * File name: Lab06.java
 * Date:      2014/08/26 21:39
 * Author:    @author
 */

package cz.cvut.fel.pjv;

import java.util.Scanner;

public class Lab02 {

   private static final int SEGMENT_SIZE = 10;
  
   public void start(String[] args) {
      homework();
   }

   private void homework(){
      TextIO io = new TextIO();
      int lineCounter = 0;

      int indexCounter = 0;
      double[] nums = new double[SEGMENT_SIZE];
      String line = io.getLine();
      ++lineCounter;

      while(line != ""){
         if(TextIO.isDouble(line)){
            nums[indexCounter++] = Double.parseDouble(line);
            if(indexCounter >= SEGMENT_SIZE){
               outputSegment(nums, indexCounter);
               indexCounter = 0;
            }
         }else{
            System.err.printf("A number has not been parsed from line %d%n", lineCounter);
         }
         line = io.getLine();
         ++lineCounter;
      }

      System.out.println("End of input detected!");
      if(indexCounter > 1){
         outputSegment(nums, indexCounter);
      }
   }

   private void outputSegment(double[] nums, int count){
      double avg = getAverage(nums, count);
      double dev = getDeviation(nums, count, avg);
      System.out.printf("%2d %.3f %.3f%n", count, avg, dev);
   }

   private double getAverage(double[] nums, int count){
      double sum = 0;
      for(int i = 0; i < count; i++){
         sum += nums[i];
      }
      return sum / count;
   }

   private double getDeviation(double[] nums, int count, double avg){
      double sumDev = 0;
      for(int i = 0; i < count; i++){
         sumDev += Math.pow(nums[i] - avg, 2);
      }
      return Math.sqrt(sumDev / count);
   }

}

/* end of Lab02.java */
