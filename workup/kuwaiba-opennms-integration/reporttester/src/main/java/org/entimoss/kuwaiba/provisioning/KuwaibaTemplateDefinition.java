package org.entimoss.kuwaiba.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KuwaibaTemplateDefinition {
   
   private String templateName = null;
   
   private String templateElementName = null;
   
   private String className = null;

   private String templateFunction = null;
   
   private List<KuwaibaTemplateDefinition> childKuwaibaTemplateDefinitions = new ArrayList<KuwaibaTemplateDefinition>();
   
   private HashMap<String,String> templateFunctionAttributes = new HashMap<String,String>();

   public KuwaibaTemplateDefinition() {
      super();
   }

   public String getTemplateName() {
      return templateName;
   }

   public void setTemplateName(String templateName) {
      this.templateName = templateName;
   }

   public String getTemplateElementName() {
      return templateElementName;
   }

   public void setTemplateElementName(String templateElementName) {
      this.templateElementName = templateElementName;
   }

   public List<KuwaibaTemplateDefinition> getChildKuwaibaTemplateDefinitions() {
      return childKuwaibaTemplateDefinitions;
   }

   public void setChildKuwaibaTemplateDefinitions(List<KuwaibaTemplateDefinition> childKuwaibaTemplateDefinitions) {
      this.childKuwaibaTemplateDefinitions = childKuwaibaTemplateDefinitions;
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getTemplateFunction() {
      return templateFunction;
   }

   public void setTemplateFunction(String templateFunction) {
      this.templateFunction = templateFunction;
   }

   public HashMap<String, String> getTemplateFunctionAttributes() {
      return templateFunctionAttributes;
   }

   public void setTemplateFunctionAttributes(HashMap<String, String> templateFunctionAttributes) {
      this.templateFunctionAttributes = templateFunctionAttributes;
   }

   

}
