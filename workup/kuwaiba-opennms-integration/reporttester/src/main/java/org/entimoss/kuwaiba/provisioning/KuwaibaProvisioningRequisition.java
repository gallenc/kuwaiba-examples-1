package org.entimoss.kuwaiba.provisioning;

import java.util.ArrayList;
import java.util.List;


public class KuwaibaProvisioningRequisition {
   
   private List<KuwaibaTemplateDefinition> kuwaibaTemplateList = new ArrayList<KuwaibaTemplateDefinition>();

   private List<KuwaibaClass> kuwaibaClassList = new ArrayList<KuwaibaClass>();
   
   private List<KuwaibaWireContainerConnection> kuwaibaWireContainerConnectionList = new ArrayList<KuwaibaWireContainerConnection>();
   
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

   public List<KuwaibaWireContainerConnection> getKuwaibaWireContainerConnectionList() {
      return kuwaibaWireContainerConnectionList;
   }

   public void setKuwaibaWireContainerConnectionList(List<KuwaibaWireContainerConnection> kuwaibaWireContainerConnectionList) {
      this.kuwaibaWireContainerConnectionList = kuwaibaWireContainerConnectionList;
   }

   @Override
   public String toString() {
      return "KuwaibaProvisioningRequisition [kuwaibaTemplateList=" + kuwaibaTemplateList + ", kuwaibaClassList=" + kuwaibaClassList + ", kuwaibaWireContainerConnectionList="
               + kuwaibaWireContainerConnectionList + "]";
   }

}
