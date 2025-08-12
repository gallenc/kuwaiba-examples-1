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

package org.entimoss.misc.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class KuwaibaProvisioningConstants {

   @Test
   public void test() {
      fail("Not yet implemented");
   }
   
   public static final String  CLASS_NAME ="class_name";
   public static final String  CLASS_TEMPLATE ="class_template";
   public static final String  NAME ="name";
   public static final String  PARENTCLASS ="parentClass";
   public static final String  PARENTNAME ="parentName";
   public static final String  LATITUDE ="latitude";
   public static final String  LONGITUDE ="longitude";
   public static final String  IPADDRESS ="ipAddress";
   public static final String  COMMENT ="comment";
   public static final String  SERIALNUMBER ="serialNumber";
   public static final String  ASSETNUMBER ="assetNumber";
   public static final String  IN ="in";
   public static final String  OUT ="out";

   public static final List<String> KUWAIBA_PROVISIONING_HEADERS = Arrays.asList("CLASS","TEMPLATE","NAME","PARENTCLASS","PARENTNAME","LATITUDE","LONGITUDE","IPADDRESS","COMMENT","SERIALNUMBER","ASSETNUMBER","IN","OUT");

}
