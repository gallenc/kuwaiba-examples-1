package org.entimoss.kuwaiba.provisioning;

import java.util.HashMap;

public class KuwaibaClass {

   private String className = null;
   private String templateName = null;
   private String name = null;
   private Boolean special = false;

   private String parentClassName = null;
   private String parentName = null;

   private HashMap<String, String> attributes = new HashMap();

   public KuwaibaClass() {
      super();
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getTemplateName() {
      return templateName;
   }

   public void setTemplateName(String templateName) {
      this.templateName = templateName;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getParentClassName() {
      return parentClassName;
   }

   public void setParentClassName(String parentClassName) {
      this.parentClassName = parentClassName;
   }

   public String getParentName() {
      return parentName;
   }

   public void setParentName(String parentName) {
      this.parentName = parentName;
   }

   public HashMap<String, String> getAttributes() {
      return attributes;
   }

   public void setAttributes(HashMap<String, String> attributes) {
      this.attributes = attributes;
   }

   public Boolean getSpecial() {
      return special;
   }

   public void setSpecial(Boolean special) {
      this.special = special;
   }

   @Override
   public String toString() {
      return "KuwaibaClass [className=" + className + ", name=" + name + ", templateName=" + templateName + ", special=" + special + 
               ", parentClassName=" + parentClassName + ", parentName=" + parentName + ", attributes=" + attributes + "]";
   }

}

