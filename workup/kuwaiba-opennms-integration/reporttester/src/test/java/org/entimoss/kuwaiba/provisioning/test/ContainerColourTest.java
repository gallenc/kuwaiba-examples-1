package org.entimoss.kuwaiba.provisioning.test;

import static org.junit.Assert.*;

import java.util.List;

import org.entimoss.kuwaiba.provisioning.ContainerColour;
import org.junit.Test;

public class ContainerColourTest {

   @Test
   public void test() {

      int circuitNo;

      //      for (circuitNo = 0; circuitNo <= 12*12*12+1; circuitNo++) {
      //         System.out.println("  test circuitno="+circuitNo + " " +circuitNo % ContainerColour.orderedFibreColours.size()+ " " +circuitNo / ContainerColour.orderedFibreColours.size());
      //      
      //      }

      for (circuitNo = 1; circuitNo <= 12; circuitNo++) {
         System.out.println("container colour list for circuitNo=" + circuitNo);
         List<String> containers = ContainerColour.getNestedContainerColourList(circuitNo);
         System.out.println("  containers=" + containers);
         assertEquals(ContainerColour.getColourForStrand(circuitNo), containers.get(containers.size()-1));
      }

      circuitNo = 13;
      System.out.println("container colour list for circuitNo=" + circuitNo);
      List<String> containers = ContainerColour.getNestedContainerColourList(circuitNo);
      System.out.println("  containers=" + containers);
      assertEquals(ContainerColour.getColourForStrand(1), containers.get(containers.size()-1));
      assertEquals(ContainerColour.getColourForStrand(2), containers.get(containers.size()-2));

      circuitNo = 14;
      System.out.println("container colour list for circuitNo=" + circuitNo);
      containers = ContainerColour.getNestedContainerColourList(circuitNo);
      System.out.println("  containers=" + containers);
      assertEquals(ContainerColour.getColourForStrand(2), containers.get(containers.size()-1));
      assertEquals(ContainerColour.getColourForStrand(2), containers.get(containers.size()-2));

      circuitNo = (ContainerColour.orderedFibreColours.size() * ContainerColour.orderedFibreColours.size() * ContainerColour.orderedFibreColours.size());
      System.out.println("container colour list for circuitNo=" + circuitNo);
      containers = ContainerColour.getNestedContainerColourList(circuitNo);
      System.out.println("  containers=" + containers);
      // assertEquals(ContainerColour.getColourForStrand(1), containers.get(0));

      circuitNo = (ContainerColour.orderedFibreColours.size() * ContainerColour.orderedFibreColours.size() * ContainerColour.orderedFibreColours.size()) - 1;
      System.out.println("container colour list for circuitNo=" + circuitNo);
      containers = ContainerColour.getNestedContainerColourList(circuitNo);
      System.out.println("  containers=" + containers);
      //(ContainerColour.getColourForStrand(1), containers.get(0));

   }

}
