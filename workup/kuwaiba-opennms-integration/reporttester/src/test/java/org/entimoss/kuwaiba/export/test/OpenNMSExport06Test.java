/*
 *  Copyright 2025 Entimoss Ltd (craig.gallen@entimoss.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.entimoss.kuwaiba.export.test;

import static org.junit.Assert.*;

import org.entimoss.kuwaiba.export.OpenNMSExport06;
import org.entimoss.kuwaiba.export.OpenNMSExport06.IpV4Cidr;
import org.junit.Test;

public class OpenNMSExport06Test {

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
