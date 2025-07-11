package org.entimoss.kuwaiba.input.tmp2;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

// note use COMMIT ON EXECUTE
//uncomment in groovy script
KuawabaSimpleTestsXX kuwaibaImport = new KuawabaSimpleTestsXX(bem, aem, scriptParameters);
return kuwaibaImport.runTask();

/**

 */

public class KuawabaSimpleTestsXX {
   static Logger LOG = LoggerFactory.getLogger(KuawabaSimpleTestsXX.class); // remove static in groovy

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy

   GraphDatabaseService connectionHandler = null; //injected in groovy

   public KuawabaSimpleTestsXX(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;
      this.connectionHandler = connectionHandler;

   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      taskResult.getMessages().add(TaskResult.createInformationMessage(
               String.format("running Script " + KuawabaSimpleTestsXX.class.getName() + " with parameters:" + parameters)));

      LOG.info("running Script " + KuawabaSimpleTestsXX.class.getName() + " with parameters:" + parameters);

      // String templateClassName = "FiberSplitter";

      //  String templateId=null;
      //  String templateElementClass;
      //  String templateElementId;

      /* TODO
       * create template with name for splitter etc
       */
      try {

         try {
            
            List<KuwaibaTemplateDefinition> kuwaibaTemplateDefinitionList = new ArrayList<KuwaibaTemplateDefinition>();
            
            // test creating templates
            // block to isolate local variables
            try {
               KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
               definition1.setTemplateName("TestFiberSplitterTemplate_1");
               definition1.setClassName("FiberSplitter");
               definition1.setSpecial(false);
               definition1.setTemplateFunction("FiberSplitterFunction");

               HashMap<String, String> attributes = new HashMap<String, String>();
               attributes.put("numberOfPorts", "4");
               definition1.setTemplateFunctionAttributes(attributes);

               kuwaibaTemplateDefinitionList.add(definition1);
            } catch (Exception e){
               throw new IllegalArgumentException("problem creating definition");
            }

            // block to isolate local variables            
            try {
               KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
               definition1.setTemplateName("TestFiberSplitterTemplate_1");
               definition1.setClassName("FiberSplitter");
               definition1.setSpecial(false);
               definition1.setTemplateFunction("FiberSplitterFunction");

               HashMap<String, String> attributes1 = new HashMap<String, String>();
               attributes1.put("numberOfPorts", "4");
               definition1.setTemplateFunctionAttributes(attributes1);

               kuwaibaTemplateDefinitionList.add(definition1);
            } catch (Exception e){
               throw new IllegalArgumentException("problem creating definition");
            }
            
            // block to isolate local variables            
            try {
               KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
               definition1.setTemplateName("ColoredFiberWireContainerTemplate_1");
               definition1.setClassName("WireContainer");
               definition1.setSpecial(false);
               definition1.setTemplateFunction("ColoredFiberWireContainerFunction");

               HashMap<String, String> attributes1 = new HashMap<String, String>();
               attributes1.put("numberOfCables", "4");
               attributes1.put("numberOfFibers", "4");
               definition1.setTemplateFunctionAttributes(attributes1);

               kuwaibaTemplateDefinitionList.add(definition1);

            } catch (Exception e){
               throw new IllegalArgumentException("problem creating definition");
            }
   
            createTemplates(kuwaibaTemplateDefinitionList);

//            String templateName = "WCTest1";
//            int numberOfCables = 4;
//            int numberOfFibers = 12;
            // createColouredOpticalFibreContainerTemplate(templateName, numberOfCables, numberOfFibers);

            //createOpticalFibreSplitterTemplate("fibre-split16", 16);

            //createOpticalSpliceBoxTemplate("splice-test1", 16);

            //            // create wirecontainer
            //            String templateId = aem.createTemplate("WireContainer", "WCTest1");
            //            String templateElementClassName = "WireContainer";
            //            String templateElementParentClassName = "WireContainer";
            //            String templateElementParentId = templateId;
            //            String templateElementNamePattern = "[sequence(1,4)]";
            //            // String templateElementClassName, String templateElementParentClassName, String templateElementParentId, String templateElementNamePattern
            //            List<String> childTemplateElementIds = Arrays
            //                     .asList(aem.createBulkSpecialTemplateElement(templateElementClassName, templateElementParentClassName, templateElementParentId, templateElementNamePattern));
            //
            //            for (String childId : childTemplateElementIds) {
            //               LOG.info("template child " + childId);
            //
            //               List<String> child2TemplateElementIds = Arrays.asList(aem.createBulkSpecialTemplateElement("OpticalLink",
            //                        "WireContainer", childId, "[sequence(1,12)]"));
            //               LOG.info("template child 2 " + childId);
            //
            //            }
            //
            //            // see if there is a template with the template name
            //            List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass(templateClassName);
            //            for (TemplateObjectLight tmplate : foundTemplates) {
            //               String tclassname = tmplate.getClassName();
            //               String templateElementId = tmplate.getId();
            //               String tname = tmplate.getName();
            //               LOG.debug("template: className=" + tclassname + " id=" + templateElementId + " name=" + tname);
            //
            //               //causes transaction fail
            //               // List<TemplateObjectLight> children = aem.getTemplateElementChildren(tclassname , templateElementId);
            //               // LOG.info("children:"+children);
            //            }

         } catch (Exception ex) {
            throw new IllegalArgumentException("problem creating template:", ex);
         }

      } catch (Exception ex) {
         LOG.error("error running task:", ex);
         taskResult.getMessages().add(TaskResult.createErrorMessage("error running task " + ex));
      }

      LOG.info("end of Script " + KuawabaSimpleTestsXX.class.getName());

      return taskResult;
   }

//   BusinessObject findOrCreateIfDoesntExist(String className, String classTemplate, String name, String parentClass, String parentClassName,
//            String latitude, String longitude, String IpAddress, String Comment, String serialNumber, String assetNumber) {
//
//      return null;
//   }

   // overloaded toString methods for BusinessObjects
   String businessObjectToString(BusinessObject bo) {
      return (bo == null) ? "BusinessObject[ null ]"
               : "BusinessObject[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" +
                        bo.getClassDisplayName() + " getAttributes()=" + bo.getAttributes() + "]";
   }

   String businessObjectToString(BusinessObjectLight bo) {
      return (bo == null) ? "BusinessObject[ null ]"
               : "BusinessObjectLight[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" +
                        bo.getClassDisplayName() + "]";
   }

   public int createChildTemplateElements(List<KuwaibaTemplateDefinition> kuwaibaTemplateElementList, String elementParentClassName, String elementParentId) {
      int templateElementsCreated = 0;

      for (KuwaibaTemplateDefinition templateElement : kuwaibaTemplateElementList) {
         try {
            String clildId = null;
            if (templateElement.getSpecial()) {
               clildId = aem.createTemplateSpecialElement(templateElement.getClassName(), elementParentClassName, elementParentId,
                        templateElement.getTemplateElementName());
            } else {
               clildId = aem.createTemplateElement(templateElement.getClassName(), elementParentClassName, elementParentId, templateElement.getTemplateElementName());
            }
            LOG.warn("created child:" + templateElement + ", clildId=" + clildId + " template element for elementParentClassName" +
                     elementParentClassName + ", elementParentId" + elementParentId);
            templateElementsCreated++;

            if (templateElement.getChildKuwaibaTemplateDefinitions() != null) {
               for (KuwaibaTemplateDefinition child : templateElement.getChildKuwaibaTemplateDefinitions()) {
                  templateElementsCreated = templateElementsCreated + createChildTemplateElements(child.getChildKuwaibaTemplateDefinitions(), templateElement.getClassName(), clildId);
               }
            }

         } catch (Exception ex) {
            throw new IllegalArgumentException("problem creating child template element for elementParentClassName" +
                     elementParentClassName + ", elementParentId" + elementParentId + " child:" + templateElement, ex);
         }

      }
      return templateElementsCreated;

   }

   public int createTemplates(List<KuwaibaTemplateDefinition> kuwaibaTemplateDefinitionList) {

      int templateElementsCreated = 0;

      for (KuwaibaTemplateDefinition kuwaibaTemplateDefinition : kuwaibaTemplateDefinitionList) {

         String className = kuwaibaTemplateDefinition.getClassName();

         HashMap<String, String> functionAttributes = kuwaibaTemplateDefinition.getTemplateFunctionAttributes();

         List<KuwaibaTemplateDefinition> childKuwaibaTemplateElements = kuwaibaTemplateDefinition.getChildKuwaibaTemplateDefinitions();

         // if templateName is set, this is a top level template and templateElementName must not be set
         // template functions can only be called for top level templates
         // recursive template definitions cannot use functions
         String templateName = kuwaibaTemplateDefinition.getTemplateName();
         // String templateElementName = kuwaibaTemplateDefinition.getTemplateElementName();

         // templateName  must be set
         if ((templateName == null || !templateName.isEmpty())) {
            new IllegalArgumentException("templateName  must be set");
         }
         if ((className == null || !className.isEmpty())) {
            new IllegalArgumentException("className  must be set");
         }

         // create new template
         String templateId = null;

         try {
            // check if template already exists
            List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass(className);
            for (TemplateObjectLight tmplate : foundTemplates) {
               if (templateName.equals(tmplate.getName())) {
                  templateId = tmplate.getId();
                  break;
               }
            }

            if (templateId != null) {
               LOG.info("template " + templateName + "already exists, will not create a new template with templateId=" + templateId);

            } else {
               // if template doesn't exist

               

               String function = kuwaibaTemplateDefinition.getTemplateFunction();

               if (function == null || function.isEmpty()) {
                  LOG.info("trying to create new template " + templateName);
                  
                  // no function so create a simple template for this class
                  templateId = aem.createTemplate(className, templateName);
                  templateElementsCreated++;
                  
                  if (childKuwaibaTemplateElements != null && !childKuwaibaTemplateElements.isEmpty()) {
                     // recursively create child templates
                     templateElementsCreated = templateElementsCreated + createChildTemplateElements(childKuwaibaTemplateElements, className, templateId);

                  }
                  LOG.info("template " + templateName + "Was created. New templateId=" + templateId);

               } else {
                  LOG.info("trying to create new template " + templateName+" from function"+ function+" with functionAttributes="+functionAttributes);
                  
                  switch (function) {

                  case "FiberSplitterFunction":
                     templateId = createOpticalFiberSplitterTemplate(className, templateName, functionAttributes);
                     break;

                  case "OpticalSpliceBoxFunction":
                     templateId = createOpticalSpliceBoxTemplate(className, templateName, functionAttributes);
                     break;

                  case "ColoredFiberWireContainerFunction":
                     templateId = createColoredOpticalFiberContainerTemplate(className, templateName, functionAttributes);
                     break;

                  default:
                     throw new IllegalArgumentException("template function does not exist: " + function);
                  }

                  LOG.info("template " + templateName + " was created using function " + function + " new templateId=" + templateId);

               }

            }

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating template name " + templateName, e);
         }

      }

      return templateElementsCreated;

   }

   public String createOpticalFiberSplitterTemplate(String className, String templateName, HashMap<String, String> functionAttributes) {

      if (!"FiberSplitter".equals(className)) {
         throw new IllegalArgumentException("cannot run FiberSplitter function for class=" + className);
      }
      if(functionAttributes.get("numberOfPorts")==null) throw new IllegalArgumentException("OpticalSplitterFunction number of ports not set ");

      try {
         Integer numberOfPorts = Integer.parseInt(functionAttributes.get("numberOfPorts"));

         LOG.info("creating optical splitter template templateName=" + templateName + " number of ports=" + numberOfPorts);

         // check if template name exists
         List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass("FiberSplitter");
         for (TemplateObjectLight tmplate : foundTemplates) {
            if (templateName.equals(tmplate.getName())) {
               throw new IllegalArgumentException("template already exists " + tmplate.getClassName() + " template name: " + tmplate.getName() +
                        " template id: " + tmplate.getId());
            }
         }

         // create FibreSplitter template
         String templateId = aem.createTemplate("FiberSplitter", templateName);

         String templateElementNamePattern = "[multiple-mirror(1," + numberOfPorts + ")]";

         // String templateElementClassName, String templateElementParentClassName, String templateElementParentId, String templateElementNamePattern
         List<String> childTemplateElementIds = Arrays
                  .asList(aem.createBulkTemplateElement("OpticalPort", "FiberSplitter", templateId, templateElementNamePattern));

         for (String childId : childTemplateElementIds) {
            LOG.info("created splitter optical port OUT template element  id=" + childId);

            // fails because getTemplateElement does not close transaction
            //TemplateObject templateObject = aem.getTemplateElement("OpticalPort", childId);
            // LOG.info("created splitter optical port name "+templateObject.getName()+" id=" +templateObject.getId());
         }

         return templateId;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating template name " + templateName, e);
      }

   }

   public String createOpticalSpliceBoxTemplate(String className, String templateName, HashMap<String, String> functionAttributes) {

      if (!"SpliceBox".equals(className)) {
         throw new IllegalArgumentException("cannot run SpliceBox function for class=" + className);
      }
      if(functionAttributes.get("numberOfPorts")==null) throw new IllegalArgumentException("SpliceBoxFunction number of ports not set ");


      try {
         Integer numberOfPorts = Integer.parseInt(functionAttributes.get("numberOfPorts"));

         LOG.info("creating optical splice box template templateName=" + templateName + " number of ports=" + numberOfPorts);
         // check if template name exists

         List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass("SpliceBox");
         for (TemplateObjectLight tmplate : foundTemplates) {
            if (templateName.equals(tmplate.getName())) {
               throw new IllegalArgumentException("template already exists " + tmplate.getClassName() + " template name: " + tmplate.getName() +
                        " template id: " + tmplate.getId());
            }
         }

         // create splice box template
         String templateId = aem.createTemplate("SpliceBox", templateName);

         //String templateElementNamePattern = "OUT-[sequence(1,"+numberOfPorts+ ")]";
         String templateElementNamePattern = "[mirror(1," + numberOfPorts + ")]";

         // String templateElementClassName, String templateElementParentClassName, String templateElementParentId, String templateElementNamePattern
         List<String> childTemplateElementIds = Arrays
                  .asList(aem.createBulkTemplateElement("OpticalPort", "SpliceBox", templateId, templateElementNamePattern));

         for (String childId : childTemplateElementIds) {
            LOG.info("created SpliceBox optical port template element  id=" + childId);

            // fails because getTemplateElement does not close transaction
            //            TemplateObject templateObject = aem.getTemplateElement("OpticalPort", childId);
            //            LOG.info("created splitter optical port name "+templateObject.getName()+" id=" +templateObject.getId());
         }

         return templateId;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating template name " + templateName, e);
      }

   }

   public String createColoredOpticalFiberContainerTemplate(String className, String templateName, HashMap<String, String> functionAttributes) {

      if (!"WireContainer".equals(className)) {
         throw new IllegalArgumentException("cannot run ColouredOpticalFibreFunction function for class=" + className);
      }

      if(functionAttributes.get("numberOfCables")==null) throw new IllegalArgumentException("OpticalFiberContainerTemplateFunction numberOfCables not set ");
      if(functionAttributes.get("numberOfFibers")==null) throw new IllegalArgumentException("OpticalFiberContainerTemplateFunction numberOfFibers not set ");

      Integer numberOfCables = Integer.parseInt(functionAttributes.get("numberOfCables"));
      Integer numberOfFibers = Integer.parseInt(functionAttributes.get("numberOfFibers"));

      LOG.info("creating wire container template templateName " + templateName + " number of cables " + numberOfCables + " number of fibers " + numberOfFibers);

      try {
         // check if template name exists

         List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass("WireContainer");
         for (TemplateObjectLight tmplate : foundTemplates) {
            if (templateName.equals(tmplate.getName())) {
               throw new IllegalArgumentException("template already exists " + tmplate.getClassName() + " template name: " + tmplate.getName() +
                        " template id: " + tmplate.getId());
            }
         }

         // create wire container template
         String templateId = aem.createTemplate("WireContainer", templateName);

         for (int cableNo = 1; cableNo <= numberOfCables; cableNo++) {
            String cableName = String.format("%02d", cableNo) + "-" + getColourForStrand(cableNo);

            // .createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName)
            String cableObjectId = aem.createTemplateSpecialElement("WireContainer", "WireContainer", templateId, cableName);
            LOG.info("created cable id=" + cableObjectId + " cable name=" + cableName);

            // create fibers inside cable
            for (int fiberNo = 1; fiberNo <= numberOfFibers; fiberNo++) {
               String fiberName = String.format("%02d", fiberNo) + "-" + getColourForStrand(fiberNo);
               String opticalLinkObjectId = aem.createTemplateSpecialElement("OpticalLink", "WireContainer", cableObjectId, fiberName);
               LOG.info("created optical link id=" + opticalLinkObjectId + " cable name=" + fiberName);
            }

         }

         return templateId;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating template name " + templateName, e);
      }

   }

   /*
    *  // https://www.thefoa.org/tech/ColCodes.htm
    *  Inside the cable or inside each tube in a loose tube cable, individual fibers will be color coded for identification
    *  1  Blue,  2  Orange, 3  Green, 4  Brown ,5  Slate, 6  White, 7  Red, 8  Black, 9  Yellow, 10    Violet, 11    Rose, 12    Aqua
    */
   static final List<String> orderedFibreColours = Arrays.asList("Blue", "Orange", "Green", "Brown", "Slate", "White", "Red", "Black", "Yellow", "Violet", "Rose", "Aqua");

   public static String getColourForStrand(int no) {
      if (no < 1 || no > orderedFibreColours.size()) {
         throw new IllegalArgumentException("strand size out of range: " + no);
      }
      return orderedFibreColours.get(no - 1);
   }

   public static int getStrandForColour(String colour) {
      int no = orderedFibreColours.indexOf(colour);
      if (no < 0)
         throw new IllegalArgumentException("unknown fibre colour: " + colour);
      return no + 1;
   }
   
   
   public static class KuwaibaClass {
   
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
   
   
   
   public static class KuwaibaProvisioningRequisition {
      
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


   
   public static class KuwaibaTemplateDefinition {
      
      private String templateName = null;
      
      private String templateElementName = null;
      
      private String className = null;
   
      private String templateFunction = null;
      
      private Boolean special = false;
      
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
      
      public Boolean getSpecial() {
         return special;
      }
   
      public void setSpecial(Boolean special) {
         this.special = special;
      }
   
      @Override
      public String toString() {
         return "KuwaibaTemplateDefinition [templateName=" + templateName + ", templateElementName=" + templateElementName +
                  ", className=" + className + ", templateFunction=" + templateFunction + ", special=" + special +
                  ", childKuwaibaTemplateDefinitions=" + childKuwaibaTemplateDefinitions + ", templateFunctionAttributes=" + templateFunctionAttributes + "]";
      }
   
   }
   

}
