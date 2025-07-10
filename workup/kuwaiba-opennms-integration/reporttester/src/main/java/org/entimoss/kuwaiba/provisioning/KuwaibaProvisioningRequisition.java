package org.entimoss.kuwaiba.provisioning;

import java.util.ArrayList;
import java.util.List;


public class KuwaibaProvisioningRequisition {
   
   private List<KuwaibaTemplateDefinition> kuwaibaTemplateList = new ArrayList<KuwaibaTemplateDefinition>();

   private List<KuwaibaClass> kuwaibaClassList = new ArrayList<KuwaibaClass>();
   
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

   @Override
   public String toString() {
      return "ProvisioningRecord [kuwaibaTemplateList=" + kuwaibaTemplateList + ", kuwaibaClassList=" + kuwaibaClassList + "]";
   }

   
}
