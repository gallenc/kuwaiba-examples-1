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
import java.util.HashMap;
import java.util.List;

public class KuwaibaClass {

   private String className = null;
   private String templateName = null;
   private String name = null;
   private Boolean special = false;

   // parent classes contain a hierarchy of classes to be searched to find parent.
   // may only contain one class
   private List<KuwaibaClass> parentClasses = new ArrayList<KuwaibaClass>();

   private HashMap<String, String> attributes = new HashMap<String, String>();

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

   public List<KuwaibaClass> getParentClasses() {
      return parentClasses;
   }

   public void setParentClasses(List<KuwaibaClass> parentClasses) {
      this.parentClasses = parentClasses;
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
      return "KuwaibaClass [ name=" + name + ", className=" + className + ", templateName=" + templateName + ", special=" + special + 
               ", parentClasses=" + parentClasses + ", attributes=" + attributes + "]";
   }

}
