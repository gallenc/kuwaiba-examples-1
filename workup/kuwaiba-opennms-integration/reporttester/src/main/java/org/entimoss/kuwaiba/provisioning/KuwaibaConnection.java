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

package org.entimoss.kuwaiba.provisioning;

public class KuwaibaConnection {
   
   private KuwaibaClass connectionClass;

   private KuwaibaClass endpointA;

   private KuwaibaClass endpointB;

   public KuwaibaConnection() {
      super();
   }

   public KuwaibaClass getConnectionClass() {
      return connectionClass;
   }

   public void setConnectionClass(KuwaibaClass connectionClass) {
      this.connectionClass = connectionClass;
   }

   public KuwaibaClass getEndpointA() {
      return endpointA;
   }

   public void setEndpointA(KuwaibaClass endpointA) {
      this.endpointA = endpointA;
   }

   public KuwaibaClass getEndpointB() {
      return endpointB;
   }

   public void setEndpointB(KuwaibaClass endpointB) {
      this.endpointB = endpointB;
   }

   @Override
   public String toString() {
      return "KuwaibaConnection [connectionClass=" + connectionClass + ", endpointA=" + endpointA + ", endpointB=" + endpointB + "]";
   }

}
