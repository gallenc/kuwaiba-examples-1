package org.entimoss.iputils.test;



import java.util.Arrays;
import java.util.List;

import org.entimoss.iputils.IpV4Cidr;

import static org.junit.Assert.*;
import org.junit.Test;

public class IpV4CidrTest {

   @Test
   public void ipV4CidrTest() {
      System.out.println("ipV4CidrTest");
      String ipv4WithCidrString = "192.168.1.1/24";
      IpV4Cidr ipV4Cidr = new IpV4Cidr(ipv4WithCidrString);
      System.out.println(ipV4Cidr.toString());
      assertTrue("192.168.1.1".equals(ipV4Cidr.getIpAddressString()));
      assertTrue("255.255.255.0".equals(ipV4Cidr.getNetMaskString()));

      ipv4WithCidrString = "192.168.1.1";
      ipV4Cidr = new IpV4Cidr(ipv4WithCidrString);
      System.out.println(ipV4Cidr.toString());
      assertTrue("192.168.1.1".equals(ipV4Cidr.getIpAddressString()));
      assertTrue("255.255.255.255".equals(ipV4Cidr.getNetMaskString()));

      Exception ex = null;

      List<String> wrongAddresses = Arrays.asList("192.168.1.1/33", "192.168.fred.0", "192.168.1.0/xxx", "192.168.1.0/24/2");
      for (String wrongIpv4WithCidrString : wrongAddresses) {
         try {
            ex = null;
            ipV4Cidr = new IpV4Cidr(wrongIpv4WithCidrString);
         } catch (Exception e) {
            ex = e;
         }
         System.out.println("Correct Exception thrown: " + ex);
         assertNotNull(ex);
      }

   }


   @Test
   public void complimentTest() {
      System.out.println("complimentTest");
      byte[] testArray = new byte[] { (byte) 0xF0, (byte) 0x0F, (byte) 0xF0, (byte) 0x0F };
      byte[] testResult = new byte[] { (byte) 0x0F, (byte) 0xF0, (byte) 0x0F, (byte) 0xF0 };
      byte[] complimentArray = IpV4Cidr.complimentByteArray(testArray);

      System.out.println("testArray: "+IpV4Cidr.bytesToHex(testArray));
      System.out.println("compliment "+IpV4Cidr.bytesToHex(complimentArray));

      assertTrue(IpV4Cidr.bytesToHex(testResult).equals(IpV4Cidr.bytesToHex(complimentArray)));
   }

   @Test
   public void andTest() {
      System.out.println("andTest");

      byte[] testArrayA = new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD };
      byte[] testArrayB = new byte[] { (byte) 0x0F, (byte) 0xF0, (byte) 0x0F, (byte) 0xF0 };
      byte[] testResult = new byte[] { (byte) 0x0A, (byte) 0xB0, (byte) 0x0C, (byte) 0xD0 };

      byte[] andArray = IpV4Cidr.andByteArrays(testArrayA, testArrayB);

      System.out.println("testArrayA: " + IpV4Cidr.bytesToHex(testArrayA));
      System.out.println("testArrayB: " + IpV4Cidr.bytesToHex(testArrayB));
      System.out.println("andArray:   " + IpV4Cidr.bytesToHex(andArray));

      assertTrue(IpV4Cidr.bytesToHex(testResult).equals(IpV4Cidr.bytesToHex(andArray)));
   }

   @Test
   public void xorTest() {
      System.out.println("xorTest");

      byte[] testArrayA = new byte[] { (byte) 0xAA, (byte) 0xAA, (byte) 0xCC, (byte) 0xCC };
      byte[] testArrayB = new byte[] { (byte) 0xAA, (byte) 0x55, (byte) 0x33, (byte) 0xCC };
      byte[] testResult = new byte[] { (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00 };

      byte[] xorArray = IpV4Cidr.xorByteArrays(testArrayA, testArrayB);

      System.out.println("testArrayA: " + IpV4Cidr.bytesToHex(testArrayA));
      System.out.println("testArrayB: " + IpV4Cidr.bytesToHex(testArrayB));
      System.out.println("xorArray:   " + IpV4Cidr.bytesToHex(xorArray));

      assertTrue(IpV4Cidr.bytesToHex(testResult).equals(IpV4Cidr.bytesToHex(xorArray)));
   }
   
   @Test
   public void orTest() {
      System.out.println("orTest");

      byte[] testArrayA = new byte[] { (byte) 0xAA, (byte) 0x55, (byte) 0xAA, (byte) 0x55 };
      byte[] testArrayB = new byte[] { (byte) 0x55, (byte) 0xAA, (byte) 0x55, (byte) 0xAA };
      byte[] testResult = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

      byte[] xorArray = IpV4Cidr.xorByteArrays(testArrayA, testArrayB);

      System.out.println("testArrayA: " + IpV4Cidr.bytesToHex(testArrayA));
      System.out.println("testArrayB: " + IpV4Cidr.bytesToHex(testArrayB));
      System.out.println("orArray:   " + IpV4Cidr.bytesToHex(xorArray));

      assertTrue(IpV4Cidr.bytesToHex(testResult).equals(IpV4Cidr.bytesToHex(xorArray)));
   }
   
   @Test
   public void bytesToStringTest() {
      System.out.println("bytesToStringTest");
      byte[] testArrayA = new byte[] { (byte) 0xAA, (byte) 0xAA, (byte) 0xCC, (byte) 0xCC };
      String testArrayABinaryStr = IpV4Cidr.bytesToBinary(testArrayA);
      System.out.println("testArrayABinaryStr:   " + testArrayABinaryStr);
      
      assertTrue("10101010101010101100110011001100".equals(testArrayABinaryStr));
      
      String testArrayAHexStr = IpV4Cidr.bytesToHex(testArrayA);
      System.out.println("testArrayAHexStr:   " + testArrayAHexStr);
      
      assertTrue("AAAACCCC".equals(testArrayAHexStr));
   }
   
   
   @Test
   public void ipV4ContainsTest(){
      System.out.println("ipV4ContainsTest");
      
      IpV4Cidr testNetwork = new IpV4Cidr("82.71.94.20/30");
      System.out.println("testNetwork: "+testNetwork);
      
      assertTrue(testNetwork.networkContainsAddress("82.71.94.20"));
      assertTrue(testNetwork.networkContainsAddress("82.71.94.21"));
      assertTrue(testNetwork.networkContainsAddress("82.71.94.22"));
      assertTrue(testNetwork.networkContainsAddress("82.71.94.23"));
      
      assertFalse(testNetwork.networkContainsAddress("82.71.94.24"));
      
      testNetwork = new IpV4Cidr("192.168.2.0/24");
      System.out.println("testNetwork: "+testNetwork);
      
      assertTrue(testNetwork.networkContainsAddress("192.168.2.1"));
      assertTrue(testNetwork.networkContainsAddress("192.168.2.254"));
   
      assertFalse(testNetwork.networkContainsAddress("192.168.0.254"));
      
      
   }

   @Test
   public void ipV4SubnetSubstitutionTest() {
      System.out.println("ipV4SubnetSubstitutionTest");

      String subnetNetSubstitution = "172.16.0.0/16=192.168.105.0/24";
      String ipv4Address = "172.16.105.20";

      String substituteAddress = IpV4Cidr.subnetIpv4Substitution(subnetNetSubstitution, ipv4Address);
      
      System.out.println("subnetNetSubstitution = "+subnetNetSubstitution);
      System.out.println("ipv4Address= "+ipv4Address);
      System.out.println("substituteAddress= " + substituteAddress);

      assertTrue("192.168.0.20".equals(substituteAddress));

   }

}
