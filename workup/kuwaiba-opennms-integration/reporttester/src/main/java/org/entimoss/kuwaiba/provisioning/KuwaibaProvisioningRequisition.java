package org.entimoss.kuwaiba.provisioning;

import java.util.ArrayList;
import java.util.List;


public class KuwaibaProvisioningRequisition {
   
   private List<KuwaibaClass> kuwaibaTemplateList = new ArrayList<KuwaibaClass>();

   private List<KuwaibaClass> kuwaibaClassList = new ArrayList<KuwaibaClass>();
   
   public KuwaibaProvisioningRequisition() {
      super();
   }
   
   public List<KuwaibaClass> getKuwaibaTemplateList() {
      return kuwaibaTemplateList;
   }

   public void setKuwaibaTemplateList(List<KuwaibaClass> kuwaibaTemplateList) {
      this.kuwaibaTemplateList = kuwaibaTemplateList;
   }
   
   public List<KuwaibaClass> getKuwaibaClassList() {
      return kuwaibaClassList;
   }

   public void setKuwaibaClassList(List<KuwaibaClass> kuwaibaClassList) {
      this.kuwaibaClassList = kuwaibaClassList;
   }

   @Override
   public String toString() {
      return "ProvisioningRecord [kuwaibaTemplateList=" + kuwaibaTemplateList + ", kuwaibaClassList=" + kuwaibaClassList + "]";
   }

   
}
