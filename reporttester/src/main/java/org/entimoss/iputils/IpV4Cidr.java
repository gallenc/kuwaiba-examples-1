package org.entimoss.iputils;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to decode IP V4 Address with or without a cidr address prefix
 */
public class IpV4Cidr {

   private String ipv4WithCidrString;
   private InetAddress netMask;
   private byte[] netMaskBytes;
   private String netMaskString;
   private byte[] netMaskComplimentBytes;
   private InetAddress ipAddress;
   private String ipAddressString;

   private InetAddress networkAddress;
   private byte[] networkAddressBytes;
   private String networkAddressString;

   private int cidrPrefix;

   public IpV4Cidr(String ipv4WithCidrString) {
      this.ipv4WithCidrString = ipv4WithCidrString;

      try {
         String[] parts = splitIPv4WithCidr(ipv4WithCidrString);
         ipAddressString = parts[0];
         if (parts.length < 2) {
            cidrPrefix = 0;
         } else {
            cidrPrefix = Integer.parseInt(parts[1]);
         }
      } catch (Exception ex) {
         throw new IllegalArgumentException("invalid ip v4 with cidr prefix: " + ipv4WithCidrString, ex);
      }

      int mask = 0xffffffff << (32 - cidrPrefix);

      int value = mask;
      netMaskBytes = new byte[] {
               (byte) (value >>> 24), (byte) (value >> 16 & 0xff), (byte) (value >> 8 & 0xff), (byte) (value & 0xff) };

      try {
         netMask = InetAddress.getByAddress(netMaskBytes);
      } catch (Exception ex) {
         throw new IllegalArgumentException("invalid ip v4 cidr prefix: " + cidrPrefix, ex);
      }

      netMaskString = netMask.getHostAddress();

      try {
         ipAddress = InetAddress.getByName(ipAddressString);

         byte[] ipAddressBytes = ipAddress.getAddress();

         networkAddressBytes = andByteArrays(ipAddressBytes, netMaskBytes);
         networkAddress = InetAddress.getByAddress(networkAddressBytes);
         networkAddressString = networkAddress.getHostAddress();

         netMaskComplimentBytes = complimentByteArray(netMaskBytes);

      } catch (Exception ex) {
         throw new IllegalArgumentException("invalid ipAddressString: " + ipAddressString, ex);
      }

   }

   /**
    * check if sub network represented by this object contains the testAddress
    * @param testAddress
    * @return
    */
   public boolean networkContainsAddress(InetAddress testAddress) {
      boolean contains = true;

      try {
         byte[] testAddressBytes = testAddress.getAddress();
         //System.out.println("xxx testAddressBytes:        " + bytesToHex(testAddressBytes) + "  " + bytesToBinary(testAddressBytes));
         //System.out.println("xxx netMaskBytes:            " + bytesToHex(netMaskBytes) + "  " + bytesToBinary(netMaskBytes));

         byte[] testAddressNetworkBytes = andByteArrays(testAddressBytes, netMaskBytes);

         //System.out.println("xxx testAddressNetworkBytes: " + bytesToHex(testAddressNetworkBytes) + "  " + bytesToBinary(testAddressNetworkBytes));
         //System.out.println("xxx networkAddressBytes:     " + bytesToHex(networkAddressBytes) + "  " + bytesToBinary(networkAddressBytes));

         byte[] xor = xorByteArrays(networkAddressBytes, testAddressNetworkBytes);

         //System.out.println("xxx xor: " + bytesToHex(xor) + "  " + bytesToBinary(xor));

         for (int x = 0; x < xor.length; x++) {
            if (xor[x] != 0) {
               contains = false;
               break;
            }
         }

      } catch (Exception ex) {
         throw new IllegalArgumentException("problem comparing inetAddress: " + ipAddressString, ex);
      }

      return contains;
   }
   
   

   /**
    * check if sub network represented by this object contains the testAddress in string form
    * @param testAddressStr
    * @return true if network contains ip address
    */
   public boolean networkContainsAddress(String testAddressStr) {
      if (testAddressStr.contains("/"))
         throw new IllegalArgumentException("test address cannot have cidr notation: " + testAddressStr);
      IpV4Cidr testAddress = new IpV4Cidr(testAddressStr);
      return networkContainsAddress(testAddress.ipAddress);

   }

   /**
    * splits ipv4 address into address and prefix and checks address with a regix
    * @param ipv4WithCidrString e.g. 192.168.1.1/24 with prefix or 192.168.1.1 without prefix
    * @return String[] parts. parts[0] = ipv4 address parts[1] = prefix
    */
   public static String[] splitIPv4WithCidr(String ipv4WithCidrString) {

      int cidrPrefix;

      String[] parts = ipv4WithCidrString.split("/");
      String ipAddressString = parts[0];

      String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(ipAddressString);
      if (!matcher.matches())
         throw new IllegalArgumentException("invalid ip v4 address: " + ipAddressString);

      try {
         if (parts.length > 2)
            throw new IllegalArgumentException();
         if (parts.length < 2) {
            cidrPrefix = 0;
         } else {
            cidrPrefix = Integer.parseInt(parts[1]);
            if (cidrPrefix < 0 || cidrPrefix > 32)
               throw new IllegalArgumentException();
         }
      } catch (Exception ex) {
         throw new IllegalArgumentException("invalid ip v4 with cidr prefix: " + ipv4WithCidrString, ex);
      }

      return parts;
   }

   public static byte[] complimentByteArray(byte[] bytes) {
      byte[] compliment = new byte[bytes.length];
      for (int x = 0; x < bytes.length; x++) {
         // int bits = (bytes[x] & 0xFF);
         int bits = Byte.toUnsignedInt(bytes[x]);
         int bitsCompliment = ~bits;
         byte byteCompliment = (byte) bitsCompliment;
         compliment[x] = byteCompliment;
      }
      return compliment;
   }

   public static byte[] andByteArrays(byte[] bytesA, byte[] bytesB) {
      if (bytesA.length != bytesB.length)
         throw new IllegalArgumentException("byte arrays not same length. bytesA " + bytesA.length + " bytesB " + bytesB.length);

      byte[] anded = new byte[bytesA.length];
      for (int x = 0; x < bytesA.length; x++) {
         int bitsA = Byte.toUnsignedInt(bytesA[x]);
         int bitsB = Byte.toUnsignedInt(bytesB[x]);
         int bitsAnd = bitsA & bitsB;
         byte byteAnd = (byte) bitsAnd;
         anded[x] = byteAnd;
      }
      return anded;
   }

   public static byte[] xorByteArrays(byte[] bytesA, byte[] bytesB) {
      if (bytesA.length != bytesB.length)
         throw new IllegalArgumentException("byte arrays not same length. bytesA " + bytesA.length + " bytesB " + bytesB.length);

      byte[] xored = new byte[bytesA.length];
      for (int x = 0; x < bytesA.length; x++) {
         int bitsA = Byte.toUnsignedInt(bytesA[x]);
         int bitsB = Byte.toUnsignedInt(bytesB[x]);
         int bitsXor = bitsA ^ bitsB;
         byte byteXor = (byte) bitsXor;
         xored[x] = byteXor;
      }
      return xored;
   }
   
   public static byte[] orByteArrays(byte[] bytesA, byte[] bytesB) {
      if (bytesA.length != bytesB.length)
         throw new IllegalArgumentException("byte arrays not same length. bytesA " + bytesA.length + " bytesB " + bytesB.length);

      byte[] ored = new byte[bytesA.length];
      for (int x = 0; x < bytesA.length; x++) {
         int bitsA = Byte.toUnsignedInt(bytesA[x]);
         int bitsB = Byte.toUnsignedInt(bytesB[x]);
         int bitsOr = bitsA | bitsB;
         byte byteXor = (byte) bitsOr;
         ored[x] = byteXor;
      }
      return ored;
   }

   /**
    * substitutes the network portion of the inputIpv4Address for the netowrk portion of the substitute address
    * For example:
    *                                 <inside subnet>=<substitute subnet>
    *  String subnetNetSubstitutionStr = "172.16.0.0/22=192.168.0.0/24";
    *  String inputIpv4AddressStr = "172.16.105.20";
    *  String substituteAddressStr= "192.168.105.20
    *  
    * @param subnetNetSubstitutionFilterStr
    * @param inputIpv4AddressStr
    * @return substituteAddressStr
    */
   public static String subnetIpv4Substitution(String subnetNetSubstitutionFilterStr, String inputIpv4AddressStr) {
      String substituteAddressStr = "";

      IpV4Cidr ipV4Address = null;
      IpV4Cidr insideSubnet = null;
      IpV4Cidr substituteSubnet = null;

      try {

         String[] parts = subnetNetSubstitutionFilterStr.split("=");
         if (parts.length != 2)
            throw new IllegalArgumentException("no '=' seperating parts in subnetNetSubstitution: " + subnetNetSubstitutionFilterStr);

         insideSubnet = new IpV4Cidr(parts[0]);
         substituteSubnet = new IpV4Cidr(parts[1]);
         ipV4Address = new IpV4Cidr(inputIpv4AddressStr);

         //System.out.println("\n ipAddress = " + ipV4Address);
         //System.out.println("\n insideSubnet = " + insideSubnet);
         //System.out.println("\n substituteSubnet = " + substituteSubnet);

         if (insideSubnet.networkContainsAddress(ipV4Address.getIpAddress())) {
            
            byte[] substituteNetmaskBytes = substituteSubnet.getNetMask().getAddress();
            byte[] complimentSubstituteNetmaskBytes = complimentByteArray(substituteNetmaskBytes);
            byte[] substituteNetworkAddressBytes = substituteSubnet.getNetworkAddress().getAddress();
            byte[] ipV4AddressBytes = ipV4Address.getNetworkAddress().getAddress();
            
            byte[] newAddressBytes = andByteArrays(ipV4AddressBytes,complimentSubstituteNetmaskBytes );
            newAddressBytes =  orByteArrays(newAddressBytes, substituteNetworkAddressBytes);
            
            InetAddress substitueAddress = InetAddress.getByAddress(newAddressBytes);
            
            substituteAddressStr = substitueAddress.getHostAddress();
            
            //System.out.println("subnet contains address using substitute address string" + substituteAddressStr);
         } else {
            substituteAddressStr = inputIpv4AddressStr;
            //System.out.println("subnet does not contain address using supplied addresss string : "+substituteAddressStr);

         }

      } catch (Exception ex) {
         throw new IllegalArgumentException("incorrectly formatted subnetNetSubstitution: " + subnetNetSubstitutionFilterStr, ex);
      }

      return substituteAddressStr;
   }

   public static String bytesToHex(byte[] bytes) {
      StringBuffer sb = new StringBuffer();
      for (byte b : bytes) {
         String st = String.format("%02X", b);
         sb.append(st);
      }
      return sb.toString();
   }

   public static String bytesToBinary(byte[] bytes) {
      StringBuffer sb = new StringBuffer();
      for (byte b : bytes) {
         String st = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
         sb.append(st);
      }
      return sb.toString();
   }

   public String getIpv4WithCidrString() {
      return ipv4WithCidrString;
   }

   public InetAddress getNetMask() {
      return netMask;
   }

   public String getNetMaskString() {
      return netMaskString;
   }

   public InetAddress getIpAddress() {
      return ipAddress;
   }

   public String getIpAddressString() {
      return ipAddressString;
   }

   public int getCidrPrefix() {
      return cidrPrefix;
   }

   public InetAddress getNetworkAddress() {
      return networkAddress;
   }

   public String getNetworkAddressString() {
      return networkAddressString;
   }

   // note in groovy do NOT start new line in string aggregation with +
   @Override
   public String toString() {
      return "IpV4Cidr [ipv4WithCidrString=" + ipv4WithCidrString + ", netMask=" + netMask + ", netMaskString=" + netMaskString +
               ", ipAddress=" + ipAddress + ", ipAddressString=" + ipAddressString + ", networkAddress=" + networkAddress +
               ", networkAddressString=" + networkAddressString + ", cidrPrefix=" + cidrPrefix + "]";
   }

}