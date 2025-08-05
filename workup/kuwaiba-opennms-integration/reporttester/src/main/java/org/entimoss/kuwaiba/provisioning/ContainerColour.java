package org.entimoss.kuwaiba.provisioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContainerColour {

   /*
    *  // https://www.thefoa.org/tech/ColCodes.htm
    *  Inside the cable or inside each tube in a loose tube cable, individual fibers will be color coded for identification
    *  1  Blue,  2  Orange, 3  Green, 4  Brown ,5  Slate, 6  White, 7  Red, 8  Black, 9  Yellow, 10    Violet, 11    Rose, 12    Aqua
    */
   public static final List<String> orderedFibreColours = Arrays.asList("Blue", "Orange", "Green", "Brown", "Slate", "White", "Red", "Black", "Yellow", "Violet", "Rose", "Aqua");

   public static String getColourForStrand(int no) {
      if (no < 1 || no > orderedFibreColours.size()) {
         throw new IllegalArgumentException("strand size out of range: " + no);
      }
      return orderedFibreColours.get(no - 1);
   }

   /**
    * Used to find the fiber container colours for nested containers for a given circuit number 
    * @param circuitNo circuit 1 .. n where n max is 12*12*12*12 - 1
    * @return returns 4 segment array of colours for each nested container corresponding to a given circuit number
    */
   public static List<String> getNestedContainerColourList(int circuitNo) {
      if (circuitNo < 1)
         throw new IllegalArgumentException("circuitNo must be greater than 0: " + circuitNo);

      ArrayList<String> containerColourList = new ArrayList<String>();
      int radix = orderedFibreColours.size();
      
      String basen = Integer.toString(circuitNo-1,radix );
      // escape %1$4s as breaks in groovy
      String paddedbasen = String.format("%1\0444s", basen).replace(' ', '0');
      
      //System.out.println(circuitNo+" basen="+basen+" paddedbasen="+paddedbasen);
      
      for(int i=0; i<paddedbasen.length(); i++) {
         String s = paddedbasen.substring(i, i+1);
         Integer colorIndex = Integer.parseInt(s, radix);
         //System.out.println("colorIndex:"+colorIndex);
         String color = orderedFibreColours.get( colorIndex );
         //System.out.println("color:"+color);
         containerColourList.add(color);
      }
      
      return  containerColourList ;

   }



   public static int getStrandForColour(String colour) {
      int no = orderedFibreColours.indexOf(colour);
      if (no < 0)
         throw new IllegalArgumentException("unknown fibre colour: " + colour);
      return no + 1;
   }

}
