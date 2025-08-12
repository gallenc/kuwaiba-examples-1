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

import java.util.ArrayList;
import java.util.List;


public class KuwaibaProvisioningRequisition {
   
   private List<KuwaibaTemplateDefinition> kuwaibaTemplateList = new ArrayList<KuwaibaTemplateDefinition>();

   private List<KuwaibaClass> kuwaibaClassList = new ArrayList<KuwaibaClass>();
   
   private List<KuwaibaConnection> kuwaibaConnectionList = new ArrayList<KuwaibaConnection>();
   
   public KuwaibaProvisioningRequisition() {
      super();
   }
   
   public List<KuwaibaTemplateDefinition> getKuwaibaTemplateList() {
      return kuwaibaTemplateList;
   }

   public void setKuwaibaTemplateList(List<KuwaibaTemplateDefinition> kuwaibaTemplateList) {
      this.kuwaibaTemplateList = kuwaibaTemplateList;
   }
   
   public List<KuwaibaClass> getKuwaibaClassList() {
      return kuwaibaClassList;
   }

   public void setKuwaibaClassList(List<KuwaibaClass> kuwaibaClassList) {
      this.kuwaibaClassList = kuwaibaClassList;
   }

   public List<KuwaibaConnection> getKuwaibaConnectionList() {
      return kuwaibaConnectionList;
   }

   public void setKuwaibaConnectionList(List<KuwaibaConnection> kuwaibaConnectionList) {
      this.kuwaibaConnectionList = kuwaibaConnectionList;
   }

   @Override
   public String toString() {
      return "KuwaibaProvisioningRequisition [kuwaibaTemplateList=" + kuwaibaTemplateList + ", kuwaibaClassList=" + kuwaibaClassList +
               ", kuwaibaConnectionList=" + kuwaibaConnectionList + "]";
   }

}
