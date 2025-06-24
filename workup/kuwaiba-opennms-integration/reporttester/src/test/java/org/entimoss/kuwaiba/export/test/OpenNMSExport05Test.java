package org.entimoss.kuwaiba.export.test;

import static org.junit.Assert.*;

import org.entimoss.kuwaiba.export.OpenNMSExport05;
import org.entimoss.kuwaiba.export.OpenNMSExport05.IpV4Cidr;
import org.junit.Test;

public class OpenNMSExport05Test {

   @Test
   public void ipV4SubnetSubstitutionTest() {
      System.out.println("ipV4SubnetSubstitutionTest");

      String subnetNetSubstitution = "172.16.0.0/16=192.168.105.0/24";
      String ipv4Address = "172.16.105.20";

      String substituteAddress = IpV4Cidr.subnetIpv4Substitution(subnetNetSubstitution, ipv4Address);
      
      System.out.println("subnetNetSubstitution = "+subnetNetSubstitution);
      System.out.println("ipv4Address= "+ipv4Address);
      System.out.println("substituteAddress= " + substituteAddress);

      assertTrue("192.168.105.20".equals(substituteAddress));

   }

}
